package bgu.spl.mics.application.services;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

public class StudentService extends MicroService {
    private Student student;
    private boolean startedAlready;


    public StudentService(String name, Student student) {
        super(name);
        this.student = student;
        startedAlready = false;

    }

    @Override
    protected void initialize() {
        subscribeBroadcast(StartBroadcast.class, (StartBroadcast t)->{
            //check if this should be started or perhaps received a double start message by accident.
            if (!startedAlready){
                startedAlready = true;
                //extract the first model needed to be trained by the student taken care of by this service.
                Model model = student.getNextPreTrained();
                Future<Model> temp = null;
                if (model != null)
                    //send TrainModelEvent.
                    temp = sendEvent(new TrainModelEvent(model));
                student.setCurrFuture(temp);
            }
        });

        subscribeEvent(FinishTrainingEvent.class, (FinishTrainingEvent t)-> {
            //reached here if the model send by the student has finished training, move on to testing the model.
            Model modelToTest = (Model)student.getCurrFuture().get();
            student.addToTrained(modelToTest);
            Future nextFuture = sendEvent(new TestModelEvent(modelToTest));
            student.setCurrFuture(nextFuture);
        });
        subscribeEvent(FinishTestingEvent.class, (FinishTestingEvent t)-> {
            Model modelToPublish = (Model)student.getCurrFuture().get();
            //publish model if the test came back with good results.
            if (modelToPublish.getResults() == Model.Results.Good){
                sendEvent(new PublishResultsEvent(student.getName(),modelToPublish ));
            }
            Model model = student.getNextPreTrained();
            //this student has more Models to train, if null there are no more models.
            if (model != null){
                Future<Model> temp = sendEvent(new TrainModelEvent(model));
                student.setCurrFuture(temp);
            }
        });
        subscribeBroadcast(PublishConferenceBroadcast.class, (PublishConferenceBroadcast t)-> {
            student.aggregatePapers(t);
        });
        subscribeBroadcast(TerminateBroadcast.class, (TerminateBroadcast t)-> {
            terminate();
        });
    }


}

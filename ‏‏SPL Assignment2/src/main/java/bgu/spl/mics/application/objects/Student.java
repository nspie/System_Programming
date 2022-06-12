package bgu.spl.mics.application.objects;

import bgu.spl.mics.Future;
import bgu.spl.mics.Message;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.services.StudentService;

import java.util.ArrayList;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Degree status;
    private int publications;
    private int papersRead;
    private ArrayList<Message> aggregatePapers;
    private ArrayList<Model> preTrainedModels;
    private StudentService studentService;
    private ArrayList<Model> trainedModels;//fixme
    //Future of current Model student is waiting info from.
    private Future currFuture;

    public Student(String name, String department, String status) {
        this.name = name;
        this.department = department;
        this.publications = 0;
        this.papersRead = 0;
        this.aggregatePapers = new ArrayList<>();
        if (status.equals(Degree.MSc.toString())) {
            this.status = Degree.MSc;
        } else {
            this.status = Degree.PhD;
        }
        this.studentService = null;
        this.preTrainedModels = null;
        trainedModels = new ArrayList<>();
    }


    //getters and setters
    public void setStudentService(StudentService studentService) {
        this.studentService = studentService;
    }
    public StudentService getStudentService() {
        return studentService;
    }
    public String getName() {
        return name;
    }
    public String getDepartment() {
        return department;
    }
    public Degree getStatus() {
        return status;
    }
    public int getPublications() {
        return publications;
    }
    public int getPapersRead() {
        return papersRead;
    }
    public ArrayList<Model> getTrainedModels() {
        return trainedModels;
    }
    public void setCurrFuture(Future currFuture) {
        this.currFuture = currFuture;
    }
    public Future getCurrFuture(){
        return currFuture;
    }
    public void setPreTrainedModels(ArrayList<Model> preTrainedModels) {
        this.preTrainedModels = preTrainedModels;
    }


    //effectively the callback function of a PublishConferenceBroadcast.
    public void aggregatePapers(PublishConferenceBroadcast publish){
        ArrayList<Model> models = publish.getPublishedModels();
        for (Model model: models){
            if (model.getStudent().equals(this)){
                publications++;
            }
            else
                papersRead++;
        }
    }
    public void addToTrained(Model m){
        trainedModels.add(m);
    }
    //get the next pretrained model. is extracted to start a TrainModelEvent. if all models are trained return null to point that there are no more models to train.
    public Model getNextPreTrained(){
        if (preTrainedModels.isEmpty()){
            return null;
        }
        return this.preTrainedModels.remove(0);
    }

}
package bgu.spl.mics.application.services;
import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.GPU;
import java.util.concurrent.CountDownLatch;


public class GPUDataService extends MicroService {
    private GPU gpu;
    private CountDownLatch countDownLatch;

    public GPUDataService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
        this.countDownLatch = null;

    }

    //getters and setters
    public GPU getGpu() {
        return gpu;
    }
    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(StartBroadcast.class, (StartBroadcast t)-> {
            //register to cluster, to be able to receive data.
            gpu.getCluster().gpuRegister(this);

            while (!gpu.getBreaker().get()) {
                Message m = gpu.awaitMission();
                //Test Model Event
                if (m.getClass().equals(TestModelEvent.class)) {
                    //extract model from event
                    TestModelEvent temp = (TestModelEvent)m;
                    //set model on current gpu and test
                    gpu.setModel(temp.getModel());
                    gpu.testModel(gpu.getModel().getStudent());
                    gpu.getModel().setStatusTested();
                    //check if need to send info to msgbus, dont send if needed to terminate.
                    if (!gpu.getBreaker().get()){
                        complete(temp, gpu.getModel());
                        sendEvent(new FinishTestingEvent(gpu.getModel().getStudent().getStudentService()));
                    }

                    //train model event
                } else if (m.getClass().equals(TrainModelEvent.class)) {
                    //extract model from event, set for current gpu to handle this model
                    TrainModelEvent temp = (TrainModelEvent)m;
                    gpu.setModel(temp.getModel());
                    gpu.getModel().getData().setDataService(this);
                    gpu.getModel().setStatusTraining();
                    gpu.getModel().setResultTraining();
                    //break Data to Batches, send to cluster for further handling
                    gpu.sendDataCluster();
                    //check if this model is not yet finished, and wait for dataBatches to proccess if so.
                    while (gpu.getModel().getData().getSize() > gpu.getModel().getData().getProcessed() && !gpu.getBreaker().get()){
                        //wait for a Databatch to start working. is a blocking method.
                        gpu.awaitDataCluster();
                        //if this gpu can take more data, take as many as possible (regarding overall capacity).
                        while (gpu.canTakeMore() && !gpu.getBreaker().get()) {
                            gpu.awaitDataCluster();
                        }
                        //process the current dataBatches in the gpu.
                        while (0 < gpu.getDataToProcess().size() && !gpu.getBreaker().get()){
                            gpu.processData();
                        }
                    }
                    //check if woken up because finished handling event or because service was instructed to terminate itself.
                    if (!gpu.getBreaker().get()){
                        gpu.getModel().setStatusTrained();
                        gpu.getModel().setResultNone();
                        complete(temp, gpu.getModel());
                        sendEvent(new FinishTrainingEvent(gpu.getModel().getStudent().getStudentService()));
                    }
                    //handle termination instruction.
                } else if (m.getClass().equals(TerminateBroadcast.class)){
                    terminate();
                    gpu.activateBreaker();
                }
                //change the current model handled to null inorder to clear the model for the next model to be handled.
                gpu.setModel(null);
            }
            terminate();
        });
        this.countDownLatch.countDown();

    }

}

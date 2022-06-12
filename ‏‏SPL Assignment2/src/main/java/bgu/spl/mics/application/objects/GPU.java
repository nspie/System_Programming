package bgu.spl.mics.application.objects;
import bgu.spl.mics.Message;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.services.GPUDataService;
import bgu.spl.mics.application.services.GPUService;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private Cluster cluster;
    private Model model;
    private LinkedBlockingQueue<DataBatch> dataToProcess;
    private int capacity;
    private GPUService service;
    private GPUDataService dataService;
    private Integer tickCount;
    private LinkedBlockingQueue<Message> missionQ;
    private AtomicBoolean breaker;
    private final int tickToProcess;
    private int gpuTimeUnitUsed;
    private Object timeLock;



    public GPU(String type, Cluster cluster) {

        this.cluster = cluster;
        this.model = null;
        this.tickCount = 0;
        this.dataToProcess = new LinkedBlockingQueue<>();
        this.service = null;
        this.dataService = null;
        this.breaker = new AtomicBoolean(false);
        this.gpuTimeUnitUsed = 0;
        this.missionQ = new LinkedBlockingQueue<>();
        timeLock = new Object();

        if (type.equals(Type.RTX3090.toString())){
            this.type = Type.RTX3090;
            this.capacity = 32;
            this.tickToProcess = 1;
        }
        else if (type.equals(Type.RTX2080.toString())){
            this.type = Type.RTX2080;
            this.capacity = 16;
            this.tickToProcess = 2;
        }
        else{
            this.type = Type.GTX1080;
            this.capacity = 8;
            this.tickToProcess = 4;
        }

    }

    //setters and getters
    public void setService(GPUService service) {
        this.service = service;
    }
    public void setDataService(GPUDataService dataService) {
        this.dataService = dataService;
    }
    public int getGpuTimeUnitUsed() {
        return this.gpuTimeUnitUsed;
    }
    public LinkedBlockingQueue<Message> getMissionQ() {
        return missionQ;
    }
    public Collection<DataBatch> getDataToProcess() {
        return dataToProcess;
    }
    public int getCapacity() {
        return capacity;
    }
    public GPUService getService() {
        return service;
    }
    public int getTickCount() {
        return tickCount;
    }
    public Cluster getCluster() {
        return cluster;
    }
    public Type getType() {
        return type;
    }
    public Model getModel() {
        return model;
    }
    public AtomicBoolean getBreaker() {
        return breaker;
    }
    /**
     * @PRE: rawData == null
     * @POST:
     * rawData != null
     * batchesLeftToProcess = rawData.
     */
    public void setModel(Model model) {
        this.model = model;
    }


    //checks if the gpu can take more dataBatches from cluster (regarding capacity and if there is data in the cluster waiting to be taken.
    public boolean canTakeMore(){
        return (!cluster.gpuQIsEmpty(dataService) && (getCapacity() > getDataToProcess().size()));
    }
    //kill services in gpuDataService waiting on ticktime in this gpu, and send a termination message to the cluster.
    public void activateBreaker() {
        //frees gpus waiting for timeTicks in case of termination of program.
        synchronized (timeLock){
            breaker.getAndSet(true);
            timeLock.notifyAll();
        }

        addMission(new TerminateBroadcast());
        //free gpus from the cluster if waiting for data.
        cluster.terminateDataServices();
    }
    //add a mission for the GpuDataService for future execution.
    public void addMission(Message e){
        missionQ.add(e);
    }
    /**
     * @PRE:
     * rawData != null
     * @POST:
     * rawData != null
     */
    public void sendDataCluster(){
        int sizeOfData = getModel().getData().getSize();
        LinkedBlockingQueue<DataBatch> output = new LinkedBlockingQueue<>();
        //break Data into Batches of 1000 pieces.
        for (int i=0; i<sizeOfData ;i = i+1000){
            DataBatch temp = new DataBatch(model.getData(), i);
            output.add(temp);
        }
        //send queue of dataBatches to the cluster.
        cluster.gpuPushDataCollection(output);
    }
    /**
     * @PRE: rawData.getProcessed < rawData.getSize()
     * @POST: dataToProcess.size() = @pre(dataToProcess.size())+1
     */
    public void awaitDataCluster() {
        //Wait for data from the cluster. this is a blocking method.
        addBatch(cluster.gpuAwaitData(dataService));
    }
    /**
     * @PRE: !dataToProcess.isEmpty()
     *       rawData.getProcessed < rawData.getSize()
     * @POST:
     * dataToProcess.size() == @pre(dataToProcess.size())-1
     * rawData.getProcessed == @pre(rawData.getProcessed)+1000
     */
    public void processData(){
        synchronized (timeLock){
            //take time needed to process a dataBatch
            int counter = tickToProcess;
            //for every tick updated, wakeup and see if enough ticks have been accumulated to finish processing of current batch.
            while (counter > 0){
                try {
                    timeLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //see if woke up because process needs to be terminated.
                if (getBreaker().get())
                    break;
                counter--;
                updateGpuTimeUnitUsed();
            }
            if (!getBreaker().get()){
                model.getData().incrementProcessed();
                dataToProcess.remove();
            }
        }
    }
    /**
     * @PRE: RawData == null
     * @POST: return random answer for testing the model.
     */
    //calculate random result of testing of model.
    public void testModel(Student student) {
        double prob = Math.random();
        String studentDeg = student.getStatus().toString();
        if (studentDeg.equals("MSc")) {
            if (prob <= 0.6) {
                model.setResultGood();
            } else {
                model.setResultBad();
            }
        } else {
            if (prob <= 0.8) {
                model.setResultGood();
            } else {
                model.setResultBad();
            }
        }
    }
    /**
     * @PRE: none
     * @POST: tickCount == @pre(tickCount)+1
     */
    public void incrementTick(){
        synchronized (timeLock){
            tickCount++;
            timeLock.notifyAll();
        }
    }
    /**
     * @PRE: none
     * @POST: dataToProcess.size() = @pre(dataToProcess.size())+1
     */
    public void addBatch(DataBatch batch){
        dataToProcess.add(batch);
    }
    /**
     * @PRE: none
     * @POST: none
     */
    public Message awaitMission(){
        Message m = null;
        try {
            m = missionQ.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return m;
    }
    public void updateGpuTimeUnitUsed() {
            this.gpuTimeUnitUsed++;
    }
}
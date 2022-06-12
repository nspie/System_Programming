package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.services.CPUService;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {
    String name;
    private int totalDataBatchProcessed;
    private int cores;
    private Collection<DataBatch> dataCollection;
    private Cluster cluster;
    private CPUService cpuService;
    private int tickCount;
    private int ticksToComplete;
    private final Object lock;
    private int cpuTimeUnitUsed;
    private AtomicBoolean breaker;

    public CPU(String name, int cores, Cluster cluster) {
        this.name = name;
        this.totalDataBatchProcessed = 0;
        this.cores = cores;
        this.dataCollection = new LinkedList<>();
        this.cluster = cluster;
        this.cpuService = null;
        this.tickCount = 0;
        this.ticksToComplete = 0;
        this.lock = new Object();
        breaker = new AtomicBoolean(false);
        this.cpuTimeUnitUsed = 0;
    }



    //getters and setters
    public int getTotalDataBatchProcessed() {
        return totalDataBatchProcessed;
    }
    public int getCpuTimeUnitUsed() {//fixme all function
        return this.cpuTimeUnitUsed;
    }
    public void setTimeService(CPUService timeService) {
        this.cpuService = timeService;
    }
    public AtomicBoolean getBreaker() {
        return breaker;
    }
    public int getCores() {
        return cores;
    }
    public Collection<DataBatch> getDataCollection() {
        return dataCollection;
    }
    public Cluster getCluster() {
        return cluster;
    }
    public CPUService getTimeService() {
        return cpuService;
    }
    public int getTickCount() {
        return tickCount;
    }


    public void updateTotalDataBatchProcessed() {
        totalDataBatchProcessed = totalDataBatchProcessed + 1;
    }

    //kill proccesses waiting for tickTime.
    public void activateBreaker() {
        synchronized (lock){
            breaker.getAndSet(true);
            lock.notifyAll();
        }
        cluster.terminateDataServices();
    }




    public void awaitData() {
        DataBatch dataBatch = this.cluster.cpuAwaitData();

        if (dataBatch.getData() == null)
            activateBreaker();
        else {
            ticksToComplete(dataBatch);
            int currentTime = this.tickCount;
            synchronized (lock) {
                while (this.tickCount < currentTime + ticksToComplete && !getBreaker().get()) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (getBreaker().get()){
                        break;
                    }
                    updateCpuTimeUnitUsed();
                }
            }
            if (!getBreaker().get()){
                sendData(dataBatch);
                updateTotalDataBatchProcessed();
            }
        }
    }

    /**
     * @PRE: dataCollection.size() == 1
     * @POST: dataCollection.size() == 0
     */
    //send Data to cluster when finished processsing current Batch.
    public void sendData(DataBatch processedData) {
        this.cluster.cpuPushData(processedData);
    }

    /**
     * @PRE: none
     * @POST: none
     */
    public int ticksToComplete(DataBatch batch) {
        if (batch.getData().getType().equals(Data.Type.Images))
            this.ticksToComplete = (32 / this.cores) * 4;
        else if (batch.getData().getType().equals(Data.Type.Text))
            this.ticksToComplete = (32 / this.cores) * 2;
        else {
            this.ticksToComplete = (32 / this.cores) ;
        }
        return this.ticksToComplete;
    }

    /**
     * @PRE: none
     * @POST: tickCount == @pre(tickCount)+1
     */
    public void updateTick() {
        synchronized (lock){
            tickCount++;
            lock.notifyAll();
        }
    }

    public void updateCpuTimeUnitUsed() {
            this.cpuTimeUnitUsed++;
    }
}
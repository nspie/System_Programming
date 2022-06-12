package bgu.spl.mics.application.objects;

import bgu.spl.mics.Message;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.services.GPUDataService;
import bgu.spl.mics.application.services.GPUService;
import java.lang.Thread;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

public class GPUTest {


    private GPU gpu;
    private GPUService service;
    private GPUDataService dataService;
    private Cluster cluster;
    private Model testModel;
    private Student studentTest;
    private Data data;
    private DataBatch dataBatch;

    @Before
    public void setUp() throws Exception {
       cluster = Cluster.getInstance();
       gpu = new GPU("RTX3090", cluster);
       service = new GPUService("service", gpu);
       data = new Data("test", 0, 1000);
       dataBatch = new DataBatch(data, 0);
       studentTest = new Student("test","testDep", "Msc" );
       dataService = new GPUDataService("dataService", gpu);
       gpu.setDataService(dataService);
       testModel = new Model("test", data, studentTest);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void activateBreaker() {
        assertEquals(gpu.getBreaker().get(), false);
        gpu.activateBreaker();
        assertEquals(gpu.getBreaker().get(), true);
    }

    @Test
    public void addMission() {
        assertTrue(gpu.getMissionQ().isEmpty());
        gpu.addMission(new TerminateBroadcast());
        assertFalse(gpu.getMissionQ().isEmpty());
    }

    @Test
    public void sendDataCluster() {
        cluster.gpuRegister(dataService);
        cluster.getDataQueue().clear();
        assertTrue(cluster.getDataQueue().isEmpty());
        gpu.setModel(testModel);
        gpu.sendDataCluster();
        assertFalse(cluster.getDataQueue().isEmpty());
    }

    @Test
    public void awaitDataCluster() {
        cluster.gpuRegister(dataService);
        assertTrue(gpu.getDataToProcess().isEmpty());
        gpu.setModel(testModel);
        data.setDataService(dataService);
        cluster.cpuPushData(dataBatch);
        gpu.awaitDataCluster();
        assertFalse(gpu.getDataToProcess().isEmpty());
    }

    @Test
    public void processData() {
        assertTrue(gpu.getDataToProcess().isEmpty());
        gpu.setModel(testModel);
        gpu.addBatch(dataBatch);
        assertFalse(gpu.getDataToProcess().isEmpty());
        Thread t1 = new Thread (() -> {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gpu.incrementTick();
            ;});
        t1.start();
        gpu.processData();
        assertTrue(gpu.getDataToProcess().isEmpty());
    }


    @Test
    public void incrementTick() {
        int counter = gpu.getTickCount();
        gpu.incrementTick();
        assertEquals(counter+1, gpu.getTickCount());
    }

    @Test
    public void addBatch() {
        int size = gpu.getDataToProcess().size();
        gpu.addBatch(dataBatch);
        assertEquals(size +1, gpu.getDataToProcess().size());
    }

    @Test
    public void awaitMission() {
        assertTrue(gpu.getDataToProcess().isEmpty());
        gpu.addBatch(dataBatch);
        TestModelEvent testModelEventExample = new TestModelEvent(testModel);
        assertFalse(gpu.getDataToProcess().isEmpty());
        Thread t1 = new Thread (() -> {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gpu.addMission(testModelEventExample);
            ;});
        t1.start();
        Message m = gpu.awaitMission();
        assertEquals(m,testModelEventExample);
    }

    @Test
    public void updateGpuTimeUnitUsed() {
        int time = gpu.getGpuTimeUnitUsed();
        gpu.updateGpuTimeUnitUsed();
        assertEquals(time +1, gpu.getGpuTimeUnitUsed());
    }


}
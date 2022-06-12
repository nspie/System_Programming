package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.services.CPUDataService;
import bgu.spl.mics.application.services.GPUDataService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

public class CPUTest {



    private CPU cpu;
    private CPUDataService dataService;
    private Cluster cluster;
    private Data data;
    private DataBatch dataBatch;
    private GPU gpu;
    private GPUDataService gpuDataService;

    @Before
    public void setUp() throws Exception {
        cluster = Cluster.getInstance();
        cpu = new CPU("test", 32, cluster);
        dataService = new CPUDataService("testService", cpu);
        data = new Data("test", 0, 1000);
        dataBatch = new DataBatch(data, 0);
        gpu = new GPU("RTX3090", cluster);
        gpuDataService = new GPUDataService("dataService", gpu);
        gpu.setDataService(gpuDataService);
        data.setDataService(gpuDataService);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void updateTotalDataBatchProcessed() {
        int counter = cpu.getTotalDataBatchProcessed();
        cpu.updateTotalDataBatchProcessed();
        assertEquals(counter +1, cpu.getTotalDataBatchProcessed());
    }

    @Test
    public void activateBreaker() {
        assertFalse(cpu.getBreaker().get());
        cpu.activateBreaker();
        assertTrue(cpu.getBreaker().get());
    }
    @Test
    public void awaitData() {
        Thread t1 = new Thread (() -> {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LinkedBlockingQueue<DataBatch> Q = new LinkedBlockingQueue();
            Q.add(dataBatch);
            cluster.gpuPushDataCollection(Q);
            cpu.activateBreaker();
            ;});
        t1.start();
        Long Timer = System.currentTimeMillis();
        cpu.awaitData();
        Timer =  System.currentTimeMillis() - Timer;
        assertTrue(Timer > 500);
    }

    @Test
    public void sendData() {
        cluster.gpuRegister(gpuDataService);
        LinkedBlockingQueue Q = cluster.getGetDataQ().get(gpuDataService);
        assertTrue(Q.isEmpty());
        cpu.sendData(dataBatch);
        assertTrue(!Q.isEmpty());
    }

    @Test
    public void ticksToComplete() {
    }

    @Test
    public void updateTick() {
    }

    @Test
    public void updateCpuTimeUnitUsed() {
    }
}
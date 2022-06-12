package bgu.spl.mics;

import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.GPUService;
import bgu.spl.mics.application.services.StudentService;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.lang.Thread;


import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

public class MessageBusImplTest {
    MessageBusImpl msgBus;
    MicroService gpuExampleService;
    GPU gpuExample;
    Cluster cluster;
    TrainModelEvent testEvent;
    TickBroadcast testBroadcast;
    Student studentTest;
    StudentService studentExampleService;
    Model testModel;
    @Before
    public void setUp() throws Exception {
        msgBus = MessageBusImpl.getInstance();
        cluster = Cluster.getInstance();
        gpuExample = new GPU("RTX2080", cluster);
        studentTest = new Student("test","testDep", "Msc" );
        testModel = new Model("test", new Data("Images", 0, 1000), studentTest);
        gpuExampleService = new GPUService("name", gpuExample);
        testEvent = new TrainModelEvent(testModel);
        studentExampleService = new StudentService("testStudentService", studentTest);
        testBroadcast = new TickBroadcast();
    }

    @After
    public void tearDown() throws Exception {
        msgBus.unregister(gpuExampleService);
    }

    @Test
    public void subscribeEvent() {
        msgBus.register(gpuExampleService);
        msgBus.subscribeEvent(testEvent.getClass(), gpuExampleService);
        msgBus.sendEvent(testEvent);
        Message msg;
        msg = msgBus.awaitMessage(gpuExampleService);
        assertEquals(msg, testEvent);
        assertTrue(msgBus.isSubscribedEvent(testEvent.getClass(), gpuExampleService));
    }

    @Test
    public void subscribeBroadcast() {
        msgBus.register(gpuExampleService);
        msgBus.subscribeBroadcast(testBroadcast.getClass(), gpuExampleService);
        msgBus.sendBroadcast(testBroadcast);
        Message msg = msgBus.awaitMessage(gpuExampleService);
        assertEquals(msg, testBroadcast);
        assertTrue(msgBus.isSubscribedBroadcast(testBroadcast.getClass(), gpuExampleService));
    }

    @Test
    public void complete() {
        msgBus.register(gpuExampleService);
        msgBus.subscribeEvent(TrainModelEvent.class, gpuExampleService);
        msgBus.register(studentExampleService);
        Future<Model> futureTest = msgBus.sendEvent(testEvent);
        testEvent.getModel().setStatusTrained();
        testEvent.getModel().setResultNone();
        msgBus.complete(testEvent, testModel);
        assertEquals(testModel, futureTest.get());
    }

    @Test
    public void sendBroadcast() {
        msgBus.register(gpuExampleService);
        msgBus.subscribeBroadcast(testBroadcast.getClass(), gpuExampleService);
        msgBus.sendBroadcast(testBroadcast);
        Message msg = msgBus.awaitMessage(gpuExampleService);
        assertEquals(msg, testBroadcast);
    }

    @Test
    public void sendEvent() {
        msgBus.register(gpuExampleService);
        msgBus.subscribeEvent(testEvent.getClass(), gpuExampleService);
        msgBus.sendEvent(testEvent);
        Message msg = msgBus.awaitMessage(gpuExampleService);
        assertEquals(msg, testEvent);
    }

    @Test
    public void register() {
        msgBus.register(gpuExampleService);
        assertTrue(msgBus.isRegistered(gpuExampleService));
    }

    @Test
    public void unregister() {
        msgBus.register(gpuExampleService);
        assertTrue(msgBus.isRegistered(gpuExampleService));
        msgBus.unregister(gpuExampleService);
        assertFalse(msgBus.isRegistered(gpuExampleService));
    }

    @Test
    public void awaitMessage() {
        msgBus.register(gpuExampleService);
        msgBus.subscribeEvent(testEvent.getClass(), gpuExampleService);
        Thread t1 = new Thread (() -> {
            try {
                currentThread().sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            msgBus.sendEvent(testEvent);
            });
        t1.start();
        Long Timer = System.currentTimeMillis();
        Message msg = msgBus.awaitMessage(gpuExampleService);
        Timer =  System.currentTimeMillis() - Timer;
        assertTrue(Timer > 1000);
        assertEquals(msg, testEvent);
    }

}
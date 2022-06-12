package bgu.spl.mics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.lang.Thread;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

public class FutureTest {

    private Future<Integer> testFuture;

    @Before
    public void setUp() throws Exception {
        testFuture = new Future<Integer>();
    }

    @After
    public void tearDown() throws Exception {
        //testFuture = new Future<Integer>(); is needed?
    }

    @Test
    public void get() {
        Thread t1 = new Thread (() -> {
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            testFuture.resolve(-1);
            ;});
        t1.start();
        Long Timer = System.currentTimeMillis();
        Integer x = testFuture.get();
        Timer =  System.currentTimeMillis() - Timer;
        assertEquals(Integer.valueOf(-1), x);
        assertTrue(Timer > 3000);
    }

    @Test
    public void resolve() {
        testFuture.resolve(-1);
        Integer x = testFuture.get();
        assertEquals(Integer.valueOf(-1), x);
    }

    @Test
    public void isDone() {
        assertFalse(testFuture.isDone());
        testFuture.resolve(-1);
        assertTrue(testFuture.isDone());
    }

    @Test
    public void testGet() {
        Thread t1 = new Thread (() -> {
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            testFuture.resolve(-1);
            ;});
        t1.start();
        Long Timer = System.currentTimeMillis();
        Integer x = testFuture.get(10000, TimeUnit.MILLISECONDS);
        Timer =  System.currentTimeMillis() - Timer;
        assertEquals(Integer.valueOf(-1), x);
        assertTrue(Timer > 4000);
        //is this enough to check the correctness of the code?
    }
}
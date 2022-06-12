package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;

import java.util.concurrent.CountDownLatch;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * in addition to sending the {@link }.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

    private GPU gpu;
    private CountDownLatch countDownLatch;

    public GPUService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
        this.countDownLatch = null;

    }

    @Override
    protected void initialize() {
        //change register to be in class microservice
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast t)-> gpu.incrementTick());
        subscribeEvent(TrainModelEvent.class, (TrainModelEvent t) -> gpu.addMission(t));
        subscribeEvent(TestModelEvent.class, (TestModelEvent t) -> gpu.addMission(t));
        subscribeBroadcast(TerminateBroadcast.class, (TerminateBroadcast t)-> {
            gpu.activateBreaker();
            terminate();
        });
        this.countDownLatch.countDown();
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }
}

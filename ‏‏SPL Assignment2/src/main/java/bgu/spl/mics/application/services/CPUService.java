package bgu.spl.mics.application.services;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;
import java.util.concurrent.CountDownLatch;

/**
 * CPU service is responsible for handling the {@link}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {

    private CPU cpu;
    private CountDownLatch countDownLatch;

    public CPUService(String name, CPU cpu) {
        super(name);
        this.cpu = cpu;
        this.countDownLatch = null;

    }
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast t)-> cpu.updateTick());
        subscribeBroadcast(TerminateBroadcast.class, (TerminateBroadcast t)-> {
            //terminate the cpuDataService in the cpu, and send a termination call to the cluster.
            cpu.activateBreaker();
            terminate();
        });
        this.countDownLatch.countDown();
    }
    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }
}

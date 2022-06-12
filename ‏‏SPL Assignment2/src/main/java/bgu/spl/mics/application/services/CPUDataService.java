package bgu.spl.mics.application.services;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.CPU;
import java.util.concurrent.CountDownLatch;

public class CPUDataService extends MicroService {

    private CPU cpu;
    private CountDownLatch countDownLatch;

    public CPUDataService(String name,  CPU cpu) {
        super(name);
        this.countDownLatch = null;
        this.cpu = cpu;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(StartBroadcast.class, (StartBroadcast t)-> {
            cpu.getCluster().cpuRegister(cpu);
            //proccess data while this service is not instructed to terminate
            while (!cpu.getCluster().getCpuKiller()) {
                cpu.awaitData();
            }
            terminate();
        });
        this.countDownLatch.countDown();
    }
    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }
}

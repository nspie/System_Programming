package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import java.util.concurrent.CountDownLatch;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link },
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {
    private ConfrenceInformation confrenceInformation;
    private CountDownLatch countDownLatch;

    public ConferenceService(String name, ConfrenceInformation confrenceInformation) {
        super(name);
        this.confrenceInformation = confrenceInformation;
        this.countDownLatch = null;
    }
    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast t) -> {
            this.confrenceInformation.updateTick();
            //if time to publish, publish the info and killService via terminate().
            if (this.confrenceInformation.getTimeTick() == this.confrenceInformation.getDate()){
                sendBroadcast(new PublishConferenceBroadcast(this.confrenceInformation.getAggregatedModels()));
                this.terminate();
            }
        });
        subscribeEvent(PublishResultsEvent.class, (PublishResultsEvent p) -> {
            this.confrenceInformation.addModel(p.getModel());
        });
        subscribeBroadcast(TerminateBroadcast.class, (TerminateBroadcast t) -> {
            this.terminate();
        });
        this.countDownLatch.countDown();
    }

}
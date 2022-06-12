package bgu.spl.mics.application.services;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.StartBroadcast;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 //* all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{

	private int destTicks;
	private int speed;
	private int currTicks;
	private Timer timer;
	private Object lock;
	private TimerTask task = new TimerTask() {
		public void run() {
			sendBroadcast(new TickBroadcast());
			currTicks++;
			//if this TimerService has exceeded the time it needs to exist, terminate itself and send everyone in the system they should terminate.
			if (currTicks >= destTicks){
				timer.cancel();
				sendBroadcast(new TerminateBroadcast());
				terminate();
			}
		}
	};
	public TimeService(int destTicks, int speed) {
		super("TimerService");
		timer = new Timer();
		this.destTicks = destTicks;
		this.speed = speed;
		currTicks = 0;
		this.lock = new Object();
	}
	@Override
	protected void initialize() {
		subscribeBroadcast(StartBroadcast.class, (StartBroadcast t)-> {
			//Start the timer
			timer.scheduleAtFixedRate(task, 0, speed);
		});
		subscribeBroadcast(TerminateBroadcast.class, (TerminateBroadcast t)-> terminate());
	}


}
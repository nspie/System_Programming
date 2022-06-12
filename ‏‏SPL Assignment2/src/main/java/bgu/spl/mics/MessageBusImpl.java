package bgu.spl.mics;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.services.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */


public class MessageBusImpl implements MessageBus {
    private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> servicesQmap;
    private ConcurrentLinkedQueue<MicroService> GPUQ;
    private ConcurrentLinkedQueue<MicroService> conferenceServicesQ;
    private ConcurrentLinkedQueue<MicroService> gpuServicesSet;
    private ConcurrentLinkedQueue<MicroService> studentServicesSet;
    private ConcurrentLinkedQueue<MicroService> conferencesSet;
    private ConcurrentLinkedQueue<MicroService> cpuServicesSet;
    private ConcurrentLinkedQueue<MicroService> startServicesSet;
    private ConcurrentLinkedQueue<MicroService> terminateServicesSet;
    private final Object GPUQLock;
    private final Object ConferenceLock;

    private static MessageBusImpl instance = null;
    private static boolean isDone = false;
    //Should only occur once (singleton)
    private MessageBusImpl() {
        this.servicesQmap = new ConcurrentHashMap<>();
        this.GPUQ = new ConcurrentLinkedQueue<>();
        this.gpuServicesSet = new ConcurrentLinkedQueue<>();
        this.studentServicesSet = new ConcurrentLinkedQueue<>();
        this.conferenceServicesQ = new ConcurrentLinkedQueue<>();
        this.conferencesSet = new ConcurrentLinkedQueue<>();
        this.cpuServicesSet = new ConcurrentLinkedQueue<>();
        this.startServicesSet = new ConcurrentLinkedQueue<>();
        this.terminateServicesSet = new ConcurrentLinkedQueue<>();
        ConferenceLock = new Object();
        GPUQLock = new Object();
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static MessageBusImpl getInstance() {
        if (!isDone) {
            synchronized (MessageBusImpl.class) {
                if (!isDone) {
                    instance = new MessageBusImpl();
                    isDone = true;
                }
            }
        }
        return instance;
    }

    /**
     * @PRE: none
     * @POST: this.isRegistered(m) == true;
     */
    @Override
    public void register(MicroService microService) {
        servicesQmap.put(microService, new LinkedBlockingQueue());
        //if this is a service that requires a startBroadCast to ignite the chain of event, add the message to its queue.
        if (microService.getClass().equals(TimeService.class) || microService.getClass().equals(StudentService.class) || microService.getClass().equals(GPUDataService.class) || microService.getClass().equals(CPUDataService.class)){
            servicesQmap.get(microService).add(new StartBroadcast());
        }
    }

    /**
     * @PRE: isSubscribedEvent(Class < ? extends Event < T > > type, MicroService m) == false;
     * @POST: isSubscribedEvent(Class < ? extends Event < T > > type, MicroService m) == true;
     */
    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService microService) {
        //the additions to the Q's here are to obtain round-robing fashion mission distribution.
        if (type.equals(TrainModelEvent.class)) {
            GPUQ.add(microService);
        } else if (type.equals(PublishResultsEvent.class)) {
            conferenceServicesQ.add(microService);
        }
    }

    /**
     * @PRE: isSubscribedBroadcast(Class < ? extends Broadcast > type, MicroService m) == false;
     * @POST: isSubscribedBroadcast(Class < ? extends Broadcast > type, MicroService m) == true;
     */

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService microService) {
        if (type.equals(PublishConferenceBroadcast.class)) {
            studentServicesSet.add(microService);
        } else if (type.equals(TickBroadcast.class)) {
            if (microService.getClass().equals(GPUService.class)) {
                gpuServicesSet.add(microService);
            } else if (microService.getClass().equals(CPUService.class)) {
                cpuServicesSet.add(microService);
            } else if (microService.getClass().equals(ConferenceService.class)) {
                conferencesSet.add(microService);
            }
        } else if (type.equals(StartBroadcast.class)) {
            startServicesSet.add(microService);
        } else if (type.equals(TerminateBroadcast.class)) {
            terminateServicesSet.add(microService);
        }
    }

    /**
     * @PRE: e.getFuture().isDone() == false;
     * @POST: e.getFuture().isDone() == true;
     * e.getFuture().get() == result;
     */

    @Override
    public <T> void complete(Event<T> e, T result) {
        Future<T> temp = e.getFuture();
        temp.resolve(result);
    }

    /**
     * @TODO
     * @PRE: none
     * @POST: the microservices Queue which are subscribed to this type of broadcast are not empty.
     */

    @Override
    public void sendBroadcast(Broadcast b) {
        if (b.getClass().equals(PublishConferenceBroadcast.class)) {
            for (MicroService microService : studentServicesSet) {
                Queue<Message> Q = getQueue(microService);
                if (Q != null)
                    Q.add(b);
            }
        } else if (b.getClass().equals(StartBroadcast.class)) {
            for (MicroService microService : startServicesSet) {
                Queue<Message> Q = getQueue(microService);
                if (Q != null)
                    Q.add(b);
            }
        } else if (b.getClass().equals(TerminateBroadcast.class)){
            for (MicroService microService: terminateServicesSet){
                Queue<Message> Q = getQueue(microService);
                if (Q != null)
                    Q.add(b);
            }
        } else {
            for (MicroService temp1 : cpuServicesSet) {
                Queue<Message> Q1 = getQueue(temp1);
                if (Q1 != null)
                    Q1.add(b);
            }
            for (MicroService temp2 : gpuServicesSet) {
                Queue<Message> Q2 = getQueue(temp2);
                if (Q2 != null)
                    Q2.add(b);
            }
            synchronized (ConferenceLock){
                for (MicroService temp3 : conferencesSet) {
                    Queue<Message> Q3 = getQueue(temp3);
                    if (Q3 != null)
                        Q3.add(b);
                }
            }
        }
    }


    /**
     * @TODO
     * @PRE: none
     * @POST: a microService's Queue contains this event.
     */

    @Override
    public <T> Future<T> sendEvent(Event<T> event) {
        if (event.getClass().equals(TrainModelEvent.class) || event.getClass().equals(TestModelEvent.class)) {
            synchronized (GPUQLock){
                MicroService temp = GPUQ.remove();
                Queue<Message> Q = getQueue(temp);
                Q.add(event);
                GPUQ.add(temp);
            }
        } else if (event.getClass().equals(PublishResultsEvent.class)) {
            synchronized (ConferenceLock){
                MicroService temp = conferenceServicesQ.remove();
                Queue<Message> Q = getQueue(temp);
                Q.add(event);
                conferenceServicesQ.add(temp);
            }
        } else if (event.getClass().equals(FinishTrainingEvent.class)) {
            MicroService temp = ((FinishTrainingEvent) event).getDest();
            Queue<Message> Q = getQueue(temp);
            Q.add(event);
        } else if (event.getClass().equals(FinishTestingEvent.class)) {
            MicroService temp = ((FinishTestingEvent) event).getDest();
            Queue<Message> Q = getQueue(temp);
            Q.add(event);
        }
        return event.getFuture();
    }


    /**
     * @PRE: none
     * @POST: this.isRegistered(m) == false;
     */
    @Override
    public void unregister(MicroService microService) {
        if (!microService.getClass().equals(ConferenceService.class)){
            servicesQmap.remove(microService);
            terminateServicesSet.remove(microService);
            if (microService.getClass().equals(CPUService.class)) {
                cpuServicesSet.remove(microService);
            } else if (microService.getClass().equals(GPUService.class)) {
                gpuServicesSet.remove(microService);
                GPUQ.remove(microService);
            } else if (microService.getClass().equals(StudentService.class)) {
                studentServicesSet.remove(microService);
            }
        }
         else if (microService.getClass().equals(ConferenceService.class)){
            synchronized (ConferenceLock){
                servicesQmap.remove(microService);
                conferenceServicesQ.remove(microService);
                conferencesSet.remove(microService);
                terminateServicesSet.remove(microService);
            }
        }
    }

    /**
     * @PRE: isRegistered(m) == true;
     * @POST: return message from m's message queue
     */
    @Override

    public Message awaitMessage(MicroService microService){
        LinkedBlockingQueue<Message> tempQ = getQueue(microService);
        Message m = null;
        try {
            m = tempQ.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return m;
    }


    /**
     * @PRE: none
     * @POST: none
     */
    public boolean isRegistered(MicroService microService) {
        return getQueue(microService) != null;
    }

    /**
     * @PRE: none
     * @POST: none
     */
    public boolean isSubscribedBroadcast(Class<? extends Broadcast> type, MicroService microService) {
        if (type.equals(PublishConferenceBroadcast.class)) {
            return studentServicesSet.contains(microService);
        } else if (type.equals(TickBroadcast.class)) {
            if (microService.getClass().equals(GPUService.class)) {
                return gpuServicesSet.contains(microService);
            } else if (microService.getClass().equals(CPUService.class)) {
                return cpuServicesSet.contains(microService);
            } else if (microService.getClass().equals(ConferenceService.class)) {
                return conferencesSet.contains(microService);
            }
        }
        return false;
    }

    /**
     * @PRE: none
     * @POST: none
     */
    public <T> boolean isSubscribedEvent(Class<? extends Event<T>> type, MicroService microService) {
        if (type.equals(PublishResultsEvent.class)) {
            return conferenceServicesQ.contains(microService);
        } else if (type.equals(TestModelEvent.class) | type.equals(TrainModelEvent.class)) {
            return GPUQ.contains(microService);
        }
        return false;
    }

    private LinkedBlockingQueue<Message> getQueue(MicroService microService) {
        return servicesQmap.get(microService);
    }

}
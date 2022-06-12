package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.services.StudentService;

public class FinishTestingEvent implements Event {

    private StudentService dest;
    private Future t;

    public FinishTestingEvent(StudentService dest){
        this.dest = dest;
        t = new Future();
    }
    //@changed
    @Override
    public Future getFuture() {
        return t;
    }


    public StudentService getDest() {
        return dest;
    }
}

package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.Message;
import bgu.spl.mics.application.services.StudentService;

public class FinishTrainingEvent implements Event {


    StudentService dest;

    public FinishTrainingEvent(StudentService dest){
        this.dest = dest;
    }

    @Override
    public Future getFuture() {
        return null;
    }


    public StudentService getDest() {
        return dest;
    }
}

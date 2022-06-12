package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

public class TestModelEvent implements Event<Model> {

    //private String result;
    private Future future;
    private Model model;

    public TestModelEvent(Model model){
        this.model = model;
        future = new Future();
    }
    public Model getModel() {
        return model;
    }
    @Override
    public Future<Model> getFuture() {
        return this.future;
    }
}
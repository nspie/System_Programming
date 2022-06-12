package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.Model;

public class TrainModelEvent implements Event<Model> {

    private Future future;
    private Model model;

    public TrainModelEvent(Model model) {
        future = new Future();
        this.model = model;
    }
    public Model getModel() {
        return model;
    }
    @Override
    public Future<Model> getFuture() {
        return this.future;
    }

}
package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.Model;

public class PublishResultsEvent implements Event<Model> {

    private Model model;
    private Future future;

    public PublishResultsEvent(String senderName, Model model) {
        this.model = model;
        future = new Future();
    }

    public Model getModel(){
        return this.model;
    }

    @Override
    public Future<Model> getFuture() {
        return this.future;
    }

}

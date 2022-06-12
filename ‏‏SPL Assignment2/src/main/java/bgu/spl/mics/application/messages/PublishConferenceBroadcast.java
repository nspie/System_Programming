package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.Model;

import java.util.ArrayList;


public class PublishConferenceBroadcast implements Broadcast {

    private String conferenceName;
    private ArrayList<Model> publishedModels;

    public PublishConferenceBroadcast(ArrayList<Model> publishModels) {
        publishedModels = publishModels;
    }

    public String getConferenceName() {
        return this.conferenceName;
    }

     public ArrayList<Model> getPublishedModels() {
      return publishedModels;
     }

}
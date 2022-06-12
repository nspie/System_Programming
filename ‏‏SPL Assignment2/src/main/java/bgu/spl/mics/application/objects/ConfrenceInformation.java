package bgu.spl.mics.application.objects;

import bgu.spl.mics.MessageBusImpl;


import java.util.ArrayList;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    private String name;
    private int date;
    private int timeTick;
    private ArrayList<Model> aggregatedModels;

    public ConfrenceInformation(String name, int date) {
        this.name = name;
        this.date = date;
        this.timeTick = 0;
        this.aggregatedModels = new ArrayList<>();
    }
    public ArrayList<Model> getAggregatedModels() {
        return aggregatedModels;
    }
    public String getName() {
        return this.name;
    }
    public int getDate() {
        return date;
    }
    public int getTimeTick() {
        return timeTick;
    }
    public void updateTick() {
        this.timeTick = timeTick + 1;
    }
    public void addModel(Model model) {
        this.aggregatedModels.add(model);
    }
}
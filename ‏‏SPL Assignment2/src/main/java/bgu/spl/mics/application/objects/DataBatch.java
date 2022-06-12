package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {
    private Data data;
    private int start_index;

    public DataBatch (Data bigData, int firstIndex){
        data = bigData;
        start_index = firstIndex;
    }
    //getters
    public Data getData(){
        return data;
    }
    public int getStart_index(){
        return start_index;
    }
}

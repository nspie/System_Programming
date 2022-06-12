package bgu.spl.mics.application.objects;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.services.GPUDataService;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    /**
     * Enum representing the Data type.
     */
    enum Type {
        Images, Text, Tabular
    }

    private Type type;
    private int processed;
    private int size;
    private GPUDataService dataService;

    public Data(String type, int processed, int size) {
        this.processed = processed;
        this.size = size;
        this.dataService = null;
        //Set Type of Data.
        if (type.equals(Data.Type.Images.toString())) {
            this.type = Data.Type.Images;
        } else if (type.equals(Data.Type.Text.toString())) {
            this.type = Data.Type.Text;
        } else {
            this.type = Data.Type.Tabular;
        }
    }

    //getter and setters
    public void setDataService(GPUDataService m){
        dataService = m;
    }
    public MicroService getDataService(){
        return dataService;
    }
    public Type getType() {
        return type;
    }
    public int getProcessed() {
        return processed;
    }
    public int getSize() {
        return size;
    }

    //called when a DataBatch is handled by the gpu.
    public void incrementProcessed(){
        processed = processed+1000;
    }

}

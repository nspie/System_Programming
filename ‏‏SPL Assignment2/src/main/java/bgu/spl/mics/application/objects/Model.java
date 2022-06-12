package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {
    //enums
    public enum Status {
        PreTrained, Trained, Training, Tested
    }
    public enum Results {
        None, Good, Training, Bad
    }

    private String name;
    private Data data;
    private Student student;
    private Results results;
    private Status status;

    public Model(String name, Data data, Student student){
        this.name = name;
        this.student = student;
        this.data = data;
        this.results = Results.None;
        this.status = Status.PreTrained;
    }


    //getters and setters
    public void setName(String name) {
        this.name = name;
    }
    public void setData(Data data) {
        this.data = data;
    }
    public void setStudent(Student student) {
        this.student = student;
    }
    public void setResultGood() {
        this.results = Results.Good;
    }
    public void setResultBad() {
        this.results = Results.Bad;
    }
    public void setResultNone() {
        this.results = Results.None;
    }
    public void setResultTraining() {
        this.results = Results.Training;
    }
    public void setStatusTrained() {
        this.status = Status.Trained;
    }
    public void setStatusTraining() {
        this.status = Status.Training;
    }
    public void setStatusTested() {
        this.status = Status.Tested;
    }
    public String getName() {
        return name;
    }
    public Data getData() {
        return data;
    }
    public Student getStudent() {
        return student;
    }
    public Results getResults() {
        return results;
    }
    public Status getStatus() {
        return status;
    }


}
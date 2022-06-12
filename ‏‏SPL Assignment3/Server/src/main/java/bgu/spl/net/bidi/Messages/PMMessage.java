package bgu.spl.net.bidi.Messages;

public class PMMessage implements Message{
    private String destinationUserName;
    private String content;
    private String sendingDateAndTime;

    public PMMessage(String destinationUserName, String content, String sendingDateAndTime) {
        this.destinationUserName = destinationUserName;
        this.content = content;
        this.sendingDateAndTime = sendingDateAndTime;
    }

    public String getDestinationUserName(){
        return destinationUserName;
    }

    public String getSendingDateAndTime(){
        return sendingDateAndTime;
    }

    public void setContent(String content){
        this.content = content;
    }


    public String getContent() {
        return content;
    }


    @Override
    public String getOptCode() {
        return null;
    }
}

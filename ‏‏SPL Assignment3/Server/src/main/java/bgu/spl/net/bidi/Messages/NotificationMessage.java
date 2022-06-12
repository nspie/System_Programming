package bgu.spl.net.bidi.Messages;

public class NotificationMessage implements Message {
    private String optCode;
    private String msgOptCode;
    private String postingUser;
    private String content;
    private String print;

    public NotificationMessage(String msgOptCode, String postingUser, String content) {
        this.optCode = "09";
        this.msgOptCode = msgOptCode;
        this.content = content;
        this.postingUser = postingUser;
        this.print = "";
        setPrint();
    }

    private void setPrint(){
        if (this.msgOptCode.equals("5"))
        print =  "Public " + this.postingUser + " " + this.content;
        else {
            print = "PM "  + this.postingUser +" " + this.content;
        }
    }


    public String getOptCode() {
        return optCode;
    }


    public String getPrint() {
        return print;
    }

}

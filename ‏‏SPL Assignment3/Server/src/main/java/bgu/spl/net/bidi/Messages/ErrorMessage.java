package bgu.spl.net.bidi.Messages;

public class ErrorMessage implements Message{
    private String optCode;
    private String msgOptCode;

    public ErrorMessage(String msgOptCode) {
        this.optCode = "11";
        this.msgOptCode = msgOptCode;
    }

    public String getOptCode() {
        return optCode;
    }

    public String getMsgOptCode() {
        return msgOptCode;
    }
}

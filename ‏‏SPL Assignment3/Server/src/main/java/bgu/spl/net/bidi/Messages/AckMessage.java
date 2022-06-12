package bgu.spl.net.bidi.Messages;

public class AckMessage implements Message{
    private String optCode;
    private String msgOptCode;
    private String content;

    public AckMessage(String msgOptCode, String content) {
        this.optCode = "10";
        this.msgOptCode = msgOptCode;
        this.content = content;
    }

    public AckMessage(String msgOptCode) {
        this.optCode = "10";
        this.msgOptCode = msgOptCode;
        this.content = "";
    }

    public String getOptCode() {
        return optCode;
    }

    public String getMsgOptCode() {
        return msgOptCode;
    }

    public String getContent() {
        return content;
    }
}

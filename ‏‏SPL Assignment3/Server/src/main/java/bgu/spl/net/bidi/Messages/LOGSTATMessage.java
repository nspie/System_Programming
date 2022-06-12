package bgu.spl.net.bidi.Messages;

public class LOGSTATMessage implements Message {
    String print;

    public LOGSTATMessage() {}

    @Override
    public String getOptCode() {
        return null;
    }
}

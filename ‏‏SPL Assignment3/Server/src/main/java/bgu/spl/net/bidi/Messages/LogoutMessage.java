package bgu.spl.net.bidi.Messages;

public class LogoutMessage implements Message{
    public LogoutMessage() {}

    @Override
    public String getOptCode() {

        return "3";
    }
}

package bgu.spl.net.bidi.Messages;

public class UnfollowMessage implements Message{
    private String  type;
    private String username;

    public UnfollowMessage(String type, String username) {
        this.type = type;
        this.username = username;
    }

    public String getUsername(){
        return username;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getOptCode() {
        return null;
    }
}

package bgu.spl.net.bidi.Messages;

public class FollowMessage implements Message {
    private String type;
    private String username;

    public FollowMessage(String type, String username) {
        this.type = type;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getOptCode() {
        return null;
    }
}

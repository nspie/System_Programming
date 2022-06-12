package bgu.spl.net.bidi.Messages;

import java.util.ArrayList;
import java.util.List;

public class PostMessage implements Message{
    private String content;
    private ArrayList<String> usernames;

    public PostMessage(String content, ArrayList<String> usernames) {
        this.content = content;
        this.usernames = usernames;
    }

    public List<String> getUsernames() {
        return usernames;
    }


    public String getContent() {
        return content;
    }

    @Override
    public String getOptCode() {
        return null;
    }
}

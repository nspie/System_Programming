package bgu.spl.net.bidi.Messages;

import java.util.ArrayDeque;


public class STATMessage implements Message{
    private ArrayDeque<String> userNames;

    public STATMessage(ArrayDeque<String> userNames) {
        this.userNames = userNames;
    }

    public ArrayDeque<String> getUserNames() {
        return userNames;
    }

    @Override
    public String getOptCode() {
        return null;
    }
}

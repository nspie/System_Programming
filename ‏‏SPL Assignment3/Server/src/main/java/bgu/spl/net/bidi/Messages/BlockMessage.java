package bgu.spl.net.bidi.Messages;

public class BlockMessage implements Message{
    private String userToBlock;

    public BlockMessage(String userToBlock) {
        this.userToBlock = userToBlock;
    }

    public String getUserToBlock() {
        return userToBlock;
    }

    @Override
    public String getOptCode() {
        return null;
    }
}

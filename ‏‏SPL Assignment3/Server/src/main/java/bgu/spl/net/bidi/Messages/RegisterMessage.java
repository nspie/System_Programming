package bgu.spl.net.bidi.Messages;

public class RegisterMessage implements Message{
    private String userName;
    private String password;
    private String Birthday;

    public RegisterMessage(String userName, String password, String birthday) {
        this.userName = userName;
        this.password = password;
        Birthday = birthday;
    }


    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getBirthday() {
        return Birthday;
    }

    @Override
    public String getOptCode() {
        return null;
    }
}

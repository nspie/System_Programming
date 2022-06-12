package bgu.spl.net.bidi.Messages;

public class LoginMessage implements Message{
    private String userName;
    private String password;
    private int captcha;

    public LoginMessage(String userName, String password, String captcha) {
        this.userName = userName;
        this.password = password;
        this.captcha = Integer.parseInt(captcha);
    }


    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public int getCaptcha() {
        return captcha;
    }

    @Override
    public String getOptCode() {
        return null;
    }
}

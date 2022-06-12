package bgu.spl.net.api;

import bgu.spl.net.bidi.Messages.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

public class BidiEncoderDecoder<T> implements MessageEncoderDecoder<Message> {
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;

    @Override
    public Message decodeNextByte(byte nextByte) {
        if (nextByte == ';') {
            return popMessage();
        }
        pushByte(nextByte);
        return null; //not msg yet
    }


    @Override
    public byte[] encode(Message message) { //uses utf8 by default
        String optCode = message.getOptCode();

        switch (optCode) {

            //Notification
            case "09": {
                return ("NOTIFICATION "  +  ((NotificationMessage) message).getPrint() + ";").getBytes();
            }

            //ACK
            case "10": {
                AckMessage message1 = (AckMessage) message;
                String msgOpt = message1.getMsgOptCode();

                //Follow--Unfollow--Block
                if (msgOpt.equals("4") | msgOpt.equals("12")) {
                    return ("ACK " + message1.getMsgOptCode() + " "
                            + message1.getContent() + ";").getBytes();
                }
                //STAT--LOGSTAT
                else if (msgOpt.equals("7") | msgOpt.equals("8")) {
                    return ("ACK " + message1.getMsgOptCode() + " " + message1.getContent() + ";").getBytes();
                }
                //Rest
                else {
                    return ("ACK " + message1.getMsgOptCode() + ";").getBytes();
                }
            }

            //ERROR
            case "11": {
                return ("ERROR " + ((ErrorMessage) message).getMsgOptCode() + ";").getBytes();
            }
        }
        return null;
    }


    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private Message popMessage() {
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        bytes = new byte[1 << 10];
        len = 0;
        String optCode = result.substring(0, 2);
        result = result.substring(2);

        switch (optCode) {
            //Register
            case "01": {
                String[] array = result.split("\0");
                String name = array[0];
                String password = array[1];
                String birthday = array[2];
                return new RegisterMessage(name, password, birthday.substring(6));
            }

            //Login
            case "02": {
                String[] array = result.split("\0");
                String name = array[0];
                String password = array[1];
                String captcha = array[2];
                return new LoginMessage(name, password, captcha);
            }

            //Logout
            case "03": {
                return new LogoutMessage();
            }

            //Follow --- UnFollow
            case "04":
                String type = result.substring(0, 1);
                String userName = result.substring(2);
                if (type.equals("0")) {
                    return new FollowMessage(type, userName);
                } else {
                    return new UnfollowMessage(type, userName);
                }


                //Post
            case "05":
                ArrayList<String> usernames = new ArrayList<>();

                for (int i = 0; i < result.length(); i = i + 1) {

                    if (result.charAt(i) == '@') {
                        String name = "";
                        i++;
                        while (i < result.length() && result.charAt(i) != ' ') {
                            name = name + result.charAt(i);
                            i = i + 1;
                        }
                        usernames.add(name);
                    }

                }
                return new PostMessage(result, usernames);


            //PM
            case "06": {
                String[] array = result.split("\0");
                String destinationUserName = array[0];
                String content = array[1];
                //String dateAndTime = array[2];
                String dateAndTime = "04-01-2022 16:23";
                return new PMMessage(destinationUserName, content, dateAndTime);

            }

            //LOGSTAT
            case "07":
                return new LOGSTATMessage();


            //STAT
            case "08": {
                String[] array = result.split("\\|");
                ArrayDeque<String> ans = new ArrayDeque<>(Arrays.asList(array));
                return new STATMessage(ans);
            }

            //Block
            case "12":
                String userToBlock = result;
                return new BlockMessage(userToBlock);
        }

        return null;
    }
}

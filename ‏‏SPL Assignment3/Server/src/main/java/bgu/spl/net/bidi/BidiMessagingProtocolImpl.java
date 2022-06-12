package bgu.spl.net.bidi;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.bidi.Messages.*;
import java.util.ArrayDeque;
import java.util.HashMap;

public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<Message> {
    private int id;
    private String age;
    private Connections<Message> connections;
    boolean shouldTerminate;
    private Cluster cluster;
    private ClientData clientData;
    private HashMap<String, Boolean> wordsFilter;
    public boolean registeredNow;
    public Message ackError;

    public BidiMessagingProtocolImpl(HashMap<String, Boolean> wordsFilter1) {
        id = -1;
        connections = null;
        shouldTerminate = false;
        cluster = Cluster.getInstance();
        registeredNow = false;
        this.wordsFilter = wordsFilter1;
        ackError = null;
    }

    @Override
    public void start(int connectionId, Connections<Message> connections) {
        this.connections = connections;
        id = connectionId;
        clientData = null;
    }

    @Override
    public void process(Message message) {
        //Register
        if (message.getClass().equals(RegisterMessage.class)) {
            if (registeredNow){
                ackError = new ErrorMessage("1");
                return;
            }
            String name = ((RegisterMessage) message).getUserName();
            // == previd if registered before
            Integer prevId = cluster.wasRegistered(name);
            //has registered before
            if (prevId != null){
                ackError = new ErrorMessage("1");
                //first time registered
            } else {
                String password = ((RegisterMessage) message).getPassword();
                int birthday = 2022 - Integer.parseInt(((RegisterMessage) message).getBirthday());
                this.age = String.valueOf(birthday);
                registeredNow = true;
                cluster.addToCluster(id, age, name, password);
                ackError = new AckMessage("1");
            }
        }

        //Login
        else if (message.getClass().equals(LoginMessage.class)) {
            if (clientData == null){
                String name = ((LoginMessage) message).getUserName();
                Integer previd = cluster.wasRegistered(name);
                if (previd != null && ((LoginMessage) message).getCaptcha() == 1) {
                    ClientData tempData = cluster.getClientData(previd);
                    if (tempData.isLoggedIn()){
                        ackError = new ErrorMessage("2");
                        return;
                    }
                    else {
                        //try to log in
                        boolean succesLogin = cluster.getClientData(previd).logIn(((LoginMessage) message).getPassword());
                        //wrong values entered
                        if (!succesLogin){
                            ackError = new ErrorMessage("2");
                            return;
                        }
                            //succesfull login
                        else {
                            ackError = new AckMessage("2");
                            //have i registered now?
                            if (!registeredNow){
                                cluster.replaceId(name, id);
                            }
                            clientData = cluster.getClientData(id);
                            cluster.addLoggedInUser(((LoginMessage) message).getUserName());
                            return;
                        }
                    }
                }
            }
             
                ackError = new ErrorMessage("2");
            
        }

        //Logout
        else if (message.getClass().equals(LogoutMessage.class)) {
            // is logged in
            if (clientData != null){
                //succeed logout correctly
                if (clientData.logOut()){
                    ackError = new AckMessage("3");
                    this.cluster.removeLoggedInUser(cluster.getClientData(id).getName());
                    return;
                }
            }
            //failed to logout correctly
            ackError = new ErrorMessage("3");
        }


        //Follow
        else if (message.getClass().equals(FollowMessage.class)) {
            if (clientData != null){
                String destName = ((FollowMessage) message).getUsername();
                Integer destId = cluster.getId(destName);
                if (destId != null){
                    ClientData destData = cluster.getClientData(destId);
                    String myName = clientData.getName();
                    if (destData.addFollower(myName) && clientData.addFollowing(destName)){
                        ackError  = new AckMessage("4", ((FollowMessage) message).getUsername());
                        return;
                    }
                }
            }
            ackError = new ErrorMessage("4");
        }
        //Unfollow
        else if (message.getClass().equals(UnfollowMessage.class)) {
            if (clientData != null){
                String destName = ((UnfollowMessage) message).getUsername();
                Integer destId = cluster.getId(destName);
                if (destId != null){
                    ClientData destData = cluster.getClientData(destId);
                    String myName = clientData.getName();
                    if (destData.removeFollower(myName) && clientData.removeFollowing(destName)){
                        ackError  = new AckMessage("4", ((UnfollowMessage) message).getUsername());
                        return;
                    }
                }
            }
            ackError = new ErrorMessage("4");
        }

        //Post
        else if (message.getClass().equals(PostMessage.class)) {
            if (clientData == null) {
                ackError = new ErrorMessage("5");
                return;
            }

            //add @username id to recipientsId
            ArrayDeque<String> recipientsNames = new ArrayDeque<>();

            for (String username : ((PostMessage) message).getUsernames()) {
                Integer tempId = cluster.getId(username);
                //username doesn't exist or clocked - skip
                if (tempId != null && !this.clientData.isBlocked(username)) {
                    recipientsNames.add(username);
                }
            }

            //add following users to recipients id, iff they aren't there already
            for (String tempName : cluster.getClientData(id).getFollowedByUsers()) {
                if (!recipientsNames.contains(tempName))
                    recipientsNames.add(tempName);
            }

            //send msg to all the recipients
            for (String tempName : recipientsNames) {
                connections.send(cluster.getId(tempName), new NotificationMessage("5", clientData.getName(), ((PostMessage) message).getContent()));
            }

            cluster.addMsgToCluster(message);
            cluster.getClientData(id).incrementNumOfPosts();
            ackError = new AckMessage("5");

        }

        //PM
        else if (message.getClass().equals(PMMessage.class)) {
            //this client is not logged in
            if (clientData == null || !cluster.getClientData(id).isLoggedIn()) {
                ackError = new ErrorMessage("6");
                //connections.send(id, new ErrorMessage("6"));
                return;
            }

            String destName = ((PMMessage) message).getDestinationUserName();
            Integer destId = cluster.getId(destName);
            //recipient isn't registered, or recipient isn't followed by this user
            if (destId == null || clientData.isBlocked(destName)) {
                setAckError(new ErrorMessage("6"));
                //connections.send(id, new ErrorMessage("6"));
                return;
            }
            //filter the message
            String filteredContent = filterWords((PMMessage) message);
            ((PMMessage) message).setContent(filteredContent);

            cluster.addMsgToCluster(message);
            setAckError(new AckMessage("6"));
            connections.send(destId, new NotificationMessage("6", clientData.getName(),
                    filteredContent + " " + ((PMMessage) message).getSendingDateAndTime()));
        }

        //Block
        else if (message.getClass().equals(BlockMessage.class)) {
            String userToBlock = ((BlockMessage) message).getUserToBlock();
            if (cluster.getId(userToBlock) == null || (this.clientData == null)) {
                setAckError(new ErrorMessage("12"));
                return;
            }

            //check if block is legal and block if is
            //block second user for symmetric

            int toBlockId = this.cluster.getId(userToBlock);
            String myName = clientData.getName();
            if (clientData.addBlockedUser(userToBlock) && cluster.getClientData(toBlockId).addBlockedUser(myName)) {
                setAckError(new AckMessage("12", ((BlockMessage) message).getUserToBlock()));
                return;
            }

            setAckError(new ErrorMessage("12"));
        }
        //STAT
        else if (message.getClass().equals(STATMessage.class)) {
            ArrayDeque<String> names = ((STATMessage) message).getUserNames();
            String contents = "";
            if (clientData == null) {
                setAckError(new ErrorMessage("8"));
                return;
            }
            int index = 0;
            for (String name : names) {
                Integer tempId = cluster.getId(name);
                //check if logged in and not blocked
                if (tempId == null || !cluster.getClientData(tempId).isLoggedIn()
                        || clientData.isBlocked(name)) {
                    setAckError(new ErrorMessage("8"));
                    return;
                }
                String content = cluster.getClientData(tempId).getStats();
                if (index > 0){
                    content = "ACK 8 " + content;
                }
                contents = contents + content + "\n";
                index++;
            }
            setAckError(new AckMessage("8", contents));
        }


        //LOGSTAT
        else if (message.getClass().equals(LOGSTATMessage.class)) {
            String contents = "";
            if (!cluster.getClientData(id).isLoggedIn()) {
                setAckError(new ErrorMessage("7"));
                return;
            }
            int index = 0;
            //send stats about all loggedin users
            for (String userName : this.cluster.getLoggedInUsers()) {
                //check if certain user is blocked
                if (clientData != null && !clientData.isBlocked(userName)) {
                    String content = cluster.getClientData(cluster.getId(userName)).getStats();
                    if (index > 0){
                        content = "ACK 7 " + content;
                    }
                    contents = contents + content + "\n";
                    index++;
                }
            }
            setAckError(new AckMessage("7", contents));
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    public String filterWords(PMMessage input) {
        String newContent = "";
        String[] toFilter = input.getContent().split(" ");
        for (String word : toFilter) {
            if (this.wordsFilter.get(word) == null) {
                newContent = newContent + word + " ";
            } else {
                newContent = newContent + "<filtered> ";
            }
        }
        return newContent;
    }

    private void setAckError(Message msg) {
        ackError = msg;
    }

    public Message getAckError() {
        return ackError;
    }

    public void terminateProtocol() {
        shouldTerminate = true;
    }

}

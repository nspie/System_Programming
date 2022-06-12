package bgu.spl.net.bidi;

import bgu.spl.net.bidi.Messages.Message;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientData {
    private int id;
    private String age;
    private String name;
    private String password;
    private int numOfPosts;
    private boolean loggedIn;
    private final Object logLock;
    private ConcurrentLinkedDeque<String> followedByUsers;
    private ConcurrentLinkedDeque<String> following;
    private ConcurrentHashMap<String, Boolean> blockList;
    private final Object synFollowAndBlock;
    private ConcurrentLinkedDeque<Message> msgQueue;

    public ClientData (int id, String age, String name, String password){
        this.id = id;
        this.age = age;
        this.name = name;
        this.numOfPosts = 0;
        this.password = password;
        this.loggedIn = false;
        this.logLock = new Object();
        this.followedByUsers = new ConcurrentLinkedDeque();
        this.following = new ConcurrentLinkedDeque<>();
        this.blockList = new ConcurrentHashMap<>();
        this.synFollowAndBlock = new Object();
        this.msgQueue = new ConcurrentLinkedDeque<Message>();
    }

    //might be faulty. check if works
    public void incrementNumOfPosts(){
        numOfPosts++;
    }

    public ConcurrentLinkedDeque<Message> getMsgQueue() {
        return msgQueue;
    }

    public String getName() {
        return name;
    }
    public Object getLogLock(){
        return logLock;
    }
    public boolean isLoggedIn(){
        return loggedIn;
    }

    public boolean logIn(String password){
        if (isLoggedIn() || !this.password.equals(password))
            return false;
        loggedIn = true;
        return true;
    }

    public boolean logOut(){
        synchronized (logLock){
            if (!isLoggedIn())
                return false;
            loggedIn = false;
            return true;
        }
    }

    public boolean addFollower(String name){
        if (!isLoggedIn() || followedByUsers.contains(name)){
            return false;
        }
        synchronized (synFollowAndBlock){
            if (blockList.get(name) != null)
                return false;
            followedByUsers.add(name);
            return true;
        }
    }

    public boolean removeFollower(String name){
        if (!isLoggedIn())
            return false;
        synchronized (synFollowAndBlock){
            return followedByUsers.remove(name);
        }
    }

    public boolean addBlockedUser(String name){
        if (!isLoggedIn() || blockList.get(name) != null){
            return false;
        }
        synchronized (synFollowAndBlock){
            followedByUsers.remove(name);
            following.remove(name);
            blockList.put(name, true);
            return true;
        }
    }

    public boolean isBlocked(String name) {
        synchronized (synFollowAndBlock){
            return blockList.get(name) != null;
        }
    }

    public ConcurrentLinkedDeque<String> getFollowedByUsers() {
        return followedByUsers;
    }

    public boolean addFollowing(String name){
        if (!isLoggedIn() || following.contains(name)){
            return false;
        }
        synchronized (synFollowAndBlock){
            if (blockList.get(name) != null)
                return false;
            following.add(name);
            return true;
        }
    }

    public boolean removeFollowing(String name){
        if (!isLoggedIn())
            return false;
        synchronized (synFollowAndBlock){
            return following.remove(name);
        }
    }

    public String getStats(){
        return  this.age + " "  + this.numOfPosts + " "  + this.followedByUsers.size() + " "
                 + this.following.size();
    }

    public Message getMsg(){
        Message ans = null;
        if (msgQueue.isEmpty()){
            synchronized (logLock){
                try {
                    logLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!msgQueue.isEmpty()){
            ans = msgQueue.removeFirst();
        }
        return ans;
    }
    public void addMsg(Message message){
        msgQueue.add(message);
        synchronized (logLock){
            logLock.notifyAll();
        }
    }
}

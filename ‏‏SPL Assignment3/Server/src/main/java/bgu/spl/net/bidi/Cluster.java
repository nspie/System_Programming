package bgu.spl.net.bidi;

import bgu.spl.net.bidi.Messages.Message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Cluster {
    private ConcurrentHashMap<Integer, ClientData> idToClientData;
    private ConcurrentHashMap<String, Integer> nameToId;
    private ConcurrentLinkedDeque<Message> msgHolder;
    private ConcurrentLinkedDeque<String> loggedInUsers;

    private static Cluster instance = null;
    private static volatile boolean isDone = false;

    private Cluster() {
        this.idToClientData = new ConcurrentHashMap<>();
        this.nameToId = new ConcurrentHashMap<>();
        this.msgHolder = new ConcurrentLinkedDeque<>();
        this.loggedInUsers = new ConcurrentLinkedDeque<>();
    }

    public static Cluster getInstance() {
        if (!isDone) {
            synchronized (Cluster.class) {
                if (!isDone) {
                    instance = new Cluster();
                    isDone = true;
                }
            }
        }
        return instance;
    }

    public ClientData getClientData(int id){
        return idToClientData.get(id);
    }
    public Integer wasRegistered(String name){
        return nameToId.get(name);

    }
    public void replaceId(String name, Integer id){
        Integer prevId = nameToId.remove(name);
        nameToId.put(name, id);
        ClientData tempData = idToClientData.remove(prevId);
        idToClientData.put(id, tempData);
    }

    public void addToCluster(int id, String age, String name, String password){
        idToClientData.put(id, new ClientData(id, age, name, password));
        nameToId.put(name, id);
    }

    public Integer getId(String name){
        return nameToId.get(name);
    }

    public boolean removeFromCluster(int id){
        return (null != idToClientData.remove(id));
    }

    public void addMsgToCluster(Message msg){
        msgHolder.add(msg);
    }

    public ConcurrentLinkedDeque<String> getLoggedInUsers() {
        return loggedInUsers;
    }

    public void addLoggedInUser(String name) {//fixme
        this.loggedInUsers.add(name);
    }

    public void removeLoggedInUser(String name) {//fixme
        this.loggedInUsers.remove(name);
    }
}

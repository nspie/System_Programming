package bgu.spl.net.bidi;
import bgu.spl.net.bidi.Messages.Message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionsImpl<T> implements Connections<T>{

    private AtomicInteger currId;
    private ConcurrentHashMap<Integer,ConnectionHandler<T>> idToHandler;
    private ConcurrentLinkedDeque<ConnectionHandler<T>> registeredUsers;
    private Cluster cluster;


    private static ConnectionsImpl instance = null;
    private static volatile boolean isDone = false;

    private ConnectionsImpl() {
        this.currId = new AtomicInteger(0);
        this.idToHandler = new ConcurrentHashMap<>();
        this.registeredUsers = new ConcurrentLinkedDeque<>();
        this.cluster = Cluster.getInstance();
    }

    public static ConnectionsImpl getInstance() {
        if (!isDone) {
            synchronized (Cluster.class) {
                if (!isDone) {
                    instance = new ConnectionsImpl();
                    isDone = true;
                }
            }
        }
        return instance;
    }

    @Override
    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> temp = idToHandler.get(connectionId);
        if (temp != null){
            temp.send(msg);
            return true;
        }
        cluster.getClientData(connectionId).addMsg((Message)msg);
        return false;
    }

    @Override
    public void broadcast(T msg) {
        for (ConnectionHandler<T> temp: registeredUsers)
            temp.send(msg);
    }

    @Override
    public void disconnect(int connectionId) {
        ConnectionHandler<T> temp = idToHandler.remove(connectionId);
        if (temp != null){
            registeredUsers.remove(temp);
        }
    }

    public int signUp(ConnectionHandler<T> connectionHandler){
        int id = currId.incrementAndGet();
        idToHandler.put(id, connectionHandler);
        registeredUsers.add(connectionHandler);
        return id;
    }



}

package bgu.spl.net.srv;

import bgu.spl.net.api.BidiEncoderDecoder;
import bgu.spl.net.bidi.*;
import bgu.spl.net.bidi.Messages.Message;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.function.Supplier;

public class TPCServer<T>{

    private final int port;
    private final Supplier<BidiMessagingProtocol<T>> protocolFactory;
    private final Supplier<BidiEncoderDecoder<T>> encdecFactory;
    private ServerSocket sock;
    private Connections<T> connections;
    private Cluster cluster;
    private HashMap<String, Boolean> wordsFilter;


    public TPCServer (int port,Supplier<BidiMessagingProtocol<T>> protocolFactory, Supplier<BidiEncoderDecoder<T>> encdecFactory) {
        this.port = port;
        connections = ConnectionsImpl.getInstance();
        cluster = Cluster.getInstance();
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
    }


    public void serve() {
        try (ServerSocket serverSock = new ServerSocket(port)) {
            System.out.println("Server started");
            this.sock = serverSock; //just to be able to close
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSock = serverSock.accept();
                ConnectionHandlerImpl<T> handler = new ConnectionHandlerImpl<T>(
                        protocolFactory.get(),
                        encdecFactory.get(),
                        clientSock, connections);
                execute(handler);
            }
        } catch (IOException ex) {

        }
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("server closed!!!");
    }


    public void close() throws IOException {
        if (sock != null)
            sock.close();
    }

    private void execute(ConnectionHandlerImpl<T> handler){
        Thread t1 = new Thread(handler::run);
        t1.start();
    }
}

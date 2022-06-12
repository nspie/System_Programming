package bgu.spl.net.bidi;

import bgu.spl.net.api.BidiEncoderDecoder;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.bidi.Messages.AckMessage;
import bgu.spl.net.bidi.Messages.ErrorMessage;
import bgu.spl.net.bidi.Messages.Message;
import bgu.spl.net.bidi.Messages.NotificationMessage;
import bgu.spl.net.srv.BlockingConnectionHandler;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;


public class ConnectionHandlerImpl<T> implements ConnectionHandler<T>{
    private BidiMessagingProtocolImpl<T> protocol;
    private final BidiEncoderDecoder encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private final Object outputLock;
    private Cluster cluster;
    private ClientData clientData;
    private Integer id;
    private Connections connections;
    private final Object logLock;
    private boolean isLoggedIn;

    public ConnectionHandlerImpl(BidiMessagingProtocol protocol, BidiEncoderDecoder messageEncoderDecoder, Socket sock, Connections<T> connections){
        this.protocol = (BidiMessagingProtocolImpl<T>) protocol;
        this.encdec = messageEncoderDecoder;
        this.sock = sock;
        this.cluster = Cluster.getInstance();
        this.connections = connections;
        this.id = null;
        this.outputLock = new Object();
        logLock = new Object();
        isLoggedIn = false;
    }


    @Override
    public void send(T msg) {
        synchronized (outputLock){
            cluster.getClientData(id).addMsg((Message)msg);
        }
    }

    @Override
    public void close() throws IOException {
        connected = false;
        protocol.terminateProtocol();
        sock.close();
    }

    public void run() {
        id = ((ConnectionsImpl)connections).signUp(this);
        protocol.start(id, connections);
        try (Socket sock = this.sock) {
            int read;
            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) { //see if need to change more shit here.
                Message nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage);
                    synchronized (logLock){
                        Message response = protocol.getAckError();
                        if (response != null){
                            out.write(encdec.encode(response));
                            out.flush();
                            if (response.getClass().equals(AckMessage.class) && ((AckMessage) response).getMsgOptCode().equals("2")){
                                isLoggedIn = true;
                                Thread t2 = new Thread(this::write);
                                t2.start();
                            } else if (response.getClass().equals(AckMessage.class) && ((AckMessage) response).getMsgOptCode().equals("3")){
                                close();
                                logLock.notifyAll();
                                isLoggedIn = false;
                            }
                        }
                    }
                }

            }
            close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public void write(){
        //will enter only if logged in first
        while (!protocol.shouldTerminate() && connected){
            Message response = cluster.getClientData(id).getMsg();
            if (!connected){
                cluster.getClientData(id).addMsg(response);
                break;
            }

            synchronized (logLock){
                try {
                    if (response == null)
                        return;
                    out.write(encdec.encode(response));
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

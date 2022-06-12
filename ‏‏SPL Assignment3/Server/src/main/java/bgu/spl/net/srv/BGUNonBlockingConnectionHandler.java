package bgu.spl.net.srv;

import bgu.spl.net.api.BidiEncoderDecoder;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.bidi.*;
import bgu.spl.net.bidi.ConnectionHandler;
import bgu.spl.net.bidi.Messages.AckMessage;
import bgu.spl.net.bidi.Messages.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class BGUNonBlockingConnectionHandler<T> implements ConnectionHandler<T> {

    private static final int BUFFER_ALLOCATION_SIZE = 1 << 13; //8k
    private static final ConcurrentLinkedQueue<ByteBuffer> BUFFER_POOL = new ConcurrentLinkedQueue<>();

    private final BidiMessagingProtocol<T> protocol;
    private final BidiEncoderDecoder<T> encdec;
    private final Queue<ByteBuffer> writeQueue = new ConcurrentLinkedQueue<>();
    private final SocketChannel chan;
    private final ReactorServer reactor;
    private AtomicBoolean isLoggedIn;
    private Cluster cluster;
    private int id;
    private ConnectionsImpl<T> connections;

    public BGUNonBlockingConnectionHandler(
            BidiEncoderDecoder<T> reader,
            BidiMessagingProtocol<T> protocol,
            SocketChannel chan,
            ReactorServer reactor, Connections<T> connections) {
        this.chan = chan;
        this.encdec = reader;
        this.protocol = protocol;
        this.reactor = reactor;
        isLoggedIn = new AtomicBoolean(false);
        cluster = Cluster.getInstance();
        this.connections = (ConnectionsImpl<T>) connections;
        id = this.connections.signUp(this);
        protocol.start(id, ConnectionsImpl.getInstance());
    }


    public boolean isLoggedIn() {
        return isLoggedIn.get();
    }

    @Override
    public void send(T msg) {
        if (!isLoggedIn.get()){
            cluster.getClientData(id).addMsg((Message)msg);
        } else {
            writeQueue.add(ByteBuffer.wrap(encdec.encode((Message)msg)));
            reactor.updateInterestedOps(chan, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
    }

    public Runnable continueread() {
        ByteBuffer buf = leaseBuffer();
        boolean success = false;
        try {
            success = chan.read(buf) != -1;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (success) {
            buf.flip();
            return () -> {
                try {
                    while (buf.hasRemaining()) {
                        Message nextMessage = encdec.decodeNextByte(buf.get());
                        if (nextMessage != null) {
                            ((BidiMessagingProtocolImpl)protocol).process(nextMessage);
                            Message response = ((BidiMessagingProtocolImpl)protocol).getAckError();
                            if (response != null) {
                                writeQueue.add(ByteBuffer.wrap(encdec.encode(response)));
                                reactor.updateInterestedOps(chan, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                                if (response.getClass().equals(AckMessage.class) && ((AckMessage) response).getMsgOptCode().equals("2")){
                                    isLoggedIn.compareAndSet(false, true);
                                    ConcurrentLinkedDeque<Message> msgQ = cluster.getClientData(id).getMsgQueue();
                                    for (Message msg: msgQ){
                                        writeQueue.add(ByteBuffer.wrap(encdec.encode(msg)));
                                    }
                                    msgQ.clear();
                                } else if (response.getClass().equals(AckMessage.class) && ((AckMessage) response).getMsgOptCode().equals("3")){
                                    isLoggedIn.compareAndSet(true, false);
                                    ((BidiMessagingProtocolImpl)protocol).terminateProtocol();

                                }
                            }
                        }
                    }
                } finally {
                    releaseBuffer(buf);
                }
            };
        } else {
            releaseBuffer(buf);
            close();
            return null;
        }

    }

    public void close() {
        try {
            chan.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isClosed() {
        return !chan.isOpen();
    }

    public void continueWrite() {
        while (!writeQueue.isEmpty()) {
            try {
                ByteBuffer top = writeQueue.peek();
                chan.write(top);
                if (top.hasRemaining()) {
                    return;
                } else {
                    writeQueue.remove();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                close();
            }
        }

        if (writeQueue.isEmpty()) {
            if (protocol.shouldTerminate()) close();
            else reactor.updateInterestedOps(chan, SelectionKey.OP_READ);
        }
    }

    private static ByteBuffer leaseBuffer() {
        ByteBuffer buff = BUFFER_POOL.poll();
        if (buff == null) {
            return ByteBuffer.allocateDirect(BUFFER_ALLOCATION_SIZE);
        }

        buff.clear();
        return buff;
    }

    private static void releaseBuffer(ByteBuffer buff) {
        BUFFER_POOL.add(buff);
    }


}

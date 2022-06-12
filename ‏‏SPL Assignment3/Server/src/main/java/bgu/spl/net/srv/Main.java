package bgu.spl.net.srv;

import bgu.spl.net.api.BidiEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.bidi.BidiMessagingProtocol;
import bgu.spl.net.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.bidi.ConnectionHandlerImpl;
import bgu.spl.net.bidi.ConnectionsImpl;
import bgu.spl.net.bidi.Messages.Message;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.function.Supplier;

public class Main {
    public static void main(String[] args){
        HashMap<String, Boolean> wordsFilter = new HashMap<>();
        String filterArray[] = {"kaki", "pipi"}; //input directly
        for (String word: filterArray){
            wordsFilter.put(word, true);
        }
        Supplier<BidiMessagingProtocol<Message>> protocolFactory = ()->new BidiMessagingProtocolImpl(wordsFilter);
        Supplier<BidiEncoderDecoder<Message>> readerFactory = ()-> new BidiEncoderDecoder();
            //reactor server option
        //ReactorServer<Message> reactorServer = new ReactorServer<Message>(3, 7777, protocolFactory, readerFactory);
        //reactorServer.serve();

            //TPC server option
//        TPCServer<Message> tpcServer = new TPCServer<>(7777, protocolFactory, readerFactory);
//        tpcServer.serve();

    }
}

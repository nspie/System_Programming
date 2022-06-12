#include <stdlib.h>
#include "../include/ConnectionHandler.h"
#include <thread>
#include "../include/inputFromKeyboard.h"
#include "../include/getFromSocket.h"

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/

int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    EncDec encDec;
    ConnectionHandler connectionHandler(host, port, encDec);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    inputFromKeyboard inputFromKeyboard1(connectionHandler);
    std::thread th1(&inputFromKeyboard::run, inputFromKeyboard1);
    getFromSocket getFromSocket1(connectionHandler);
    std::thread th2(&getFromSocket::run, getFromSocket1);
    th1.join();
    th2.join();
}

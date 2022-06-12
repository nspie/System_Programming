
#include "../include/getFromSocket.h"
getFromSocket::getFromSocket(ConnectionHandler &connectionHandler1) : connectionHandler(&connectionHandler1) {
}

void getFromSocket::run() {
    while(true){
        std::string answer;
        // Get back an answer: by using the expected number of bytes (len bytes + newline delimiter)
        // We could also use: connectionHandler.getline(answer) and then get the answer without the newline char at the end
        if (!connectionHandler->getLine(answer)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        int len=answer.length();
        //deletes the ; char from the end of the string
        answer.resize(len-1);
        std::cout << answer << std::endl;
        if (answer == "ACK 3") {
            std::cout << "Exiting...\n" << std::endl;
            break;
        }
    }
}

#include <iostream>
#include "../include/inputFromKeyboard.h"

using namespace std;

inputFromKeyboard::inputFromKeyboard(ConnectionHandler &connectionHandler1) : connectionHandler(&connectionHandler1) , loggedIn(false){
}

void inputFromKeyboard::run() {
    while (true) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        if (!connectionHandler->sendLine(line)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        // connectionHandler.sendLine(line) appends '\n' to the message. Therefor we send len+1 bytes.
        if (line.length() >= 5) {
            if (line.substr(0, 5) == "LOGIN") {
                loggedIn = true;
            }
        }
        if (line == "LOGOUT" && loggedIn)
            break;
    }


}
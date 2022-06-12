//
// Created by spl211 on 02/01/2022.
//
#ifndef ASSIGNMENT3_INPUTFROMKEYBOARD_H
#define ASSIGNMENT3_INPUTFROMKEYBOARD_H

#include "ConnectionHandler.h"

class inputFromKeyboard {
private:
    ConnectionHandler *connectionHandler;
    bool loggedIn;
public:
    inputFromKeyboard(ConnectionHandler &connectionHandler1);
    void run();
};


#endif //ASSIGNMENT3_INPUTFROMKEYBOARD_H

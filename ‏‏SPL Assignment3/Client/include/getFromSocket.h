//
// Created by spl211 on 03/01/2022.
//

#ifndef ASSIGNMENT3_GETFROMSOCKET_H
#define ASSIGNMENT3_GETFROMSOCKET_H


#include "ConnectionHandler.h"

class getFromSocket {
private:
    ConnectionHandler *connectionHandler;
public:
    getFromSocket(ConnectionHandler &connectionHandler1);
    void run();
};


#endif //ASSIGNMENT3_GETFROMSOCKET_H

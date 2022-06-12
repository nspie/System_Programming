CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lboost_thread -pthread

all: echoClient
	g++ -o bin/echoClient bin/echoClient.o bin/ConnectionHandler.o bin/getFromSocket.o bin/inputFromKeyboard.o bin/EncDec.o $(LDFLAGS)

echoClient: bin/echoClient.o bin/ConnectionHandler.o bin/echoClient.o bin/inputFromKeyboard.o bin/getFromSocket.o bin/EncDec.o
	@echo 'Building target: main'
	@echo 'Invoking: C++ Linker'
	g++ -o bin/echoClient bin/ConnectionHandler.o bin/inputFromKeyboard.o bin/getFromSocket.o bin/EncDec.o bin/echoClient.o $(LDFLAGS)
	@echo 'Finished building target: main'
	@echo ' '

bin/echoClient.o: src/echoClient.cpp
	g++ $(CFLAGS) -o bin/echoClient.o src/echoClient.cpp

bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp

bin/inputFromKeyboard.o: src/inputFromKeyboard.cpp
	g++ $(CFLAGS) -o bin/inputFromKeyboard.o src/inputFromKeyboard.cpp

bin/getFromSocket.o: src/getFromSocket.cpp
	g++ $(CFLAGS) -o bin/getFromSocket.o src/getFromSocket.cpp

bin/EncDec.o: src/EncDec.cpp
	g++ $(CFLAGS) -o bin/EncDec.o src/EncDec.cpp


.PHONY: clean
clean:
	rm -f bin/*
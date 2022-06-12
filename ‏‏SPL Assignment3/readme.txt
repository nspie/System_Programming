
1)How to run your code 
Server:
mvn clean
mvn compile

TPC:
mvn exec:java -Dexec.mainClass="bgu.spl.net.srv.TPCMain" -Dexec.args="7777"

REACTOR
mvn exec:java -Dexec.mainClass="bgu.spl.net.srv.ReactorMain" -Dexec.args="7777 5"

2) where in the code you store the filtered set of words - Main Classes of each Server


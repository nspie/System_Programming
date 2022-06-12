
#include <string>
#include "../include/EncDec.h"

using namespace std;

string EncDec::encode(string toDecode) {

    string opt = toDecode.substr(0, 4);
    //Register
    if (opt == "REGI") {
        string ans = "01";
        string msg = toDecode.substr(9);
        for (int i = 0; (unsigned) i < msg.length(); i = i + 1) {
            if (msg.at(i) == ' ') {
                ans = ans + '\0';
            } else {
                ans = ans + msg.at(i);
            }
        }
        ans = ans + ";";
        return ans;
    }

    //Login
    if (opt == "LOGI") {
        string ans = "02";
        string msg = toDecode.substr(6);
        for (int i = 0; (unsigned) i < msg.length(); i = i + 1) {
            if (msg.at(i) == ' ') {
                ans = ans + '\0';
            } else {
                ans = ans + msg.at(i);
            }
        }
        ans = ans + ";";
        return ans;
    }


    //Logout
    if (opt == "LOGO") {
        string ans = "03;";
        return ans;
    }

    //Follow --- Unfollow
    if (opt == "FOLL") {
        string ans = "04";
        string msg = toDecode.substr(7);
        for (int i = 0; (unsigned) i < msg.length(); i = i + 1) {
            if (msg.at(i) == ' ') {
                ans = ans + '\0';
            } else {
                ans = ans + msg.at(i);
            }
        }
        ans = ans + ";";
        return ans;
    }

    //Post
    if (opt == "POST") {
        string ans = "05";
        string msg = toDecode.substr(5);
        bool flag = false;
        for (int i = 0; (unsigned) i < msg.length(); i = i + 1) {
            if (msg.at(i) == ' ' && !flag) {
                ans = ans + ' ';
                flag = true;
            } else {
                ans = ans + msg.at(i);
            }
        }
        ans = ans + ";";
        return ans;
    }

    //PM
    if (toDecode.substr(0, 2) == "PM") {
        string ans = "06";
        string msg = toDecode.substr(3);
        bool flag = false;
        for (int i = 0; (unsigned) i < msg.length(); i = i + 1) {
            if (msg.at(i) == ' ' && !flag) {
                ans = ans + '\0';
                flag = true;
            } else {
                ans = ans + msg.at(i);
            }
        }
        ans = ans + ";";
        return ans;
    }


    //Block
    if (opt == "BLOC") {
        string ans = "12" + toDecode.substr(6) + +";";
        return ans;
    }

    //LOGSTAT
    if (opt == "LOGS") {
        return "07;";
    }

    //STAT
    if (opt == "STAT") {
        string ans = "08";
        string msg = toDecode.substr(5);
        ans = ans + msg + ";";
        return ans;
    }

    return nullptr;
}
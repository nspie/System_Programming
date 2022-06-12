#include "Studio.h"
#include <iostream>
#include <fstream>
#include <sstream>
using namespace std;
Studio::Studio(){
}
Studio::Studio(const std::string &configFilePath) : trainers(), workout_options(), actionsLog(){
    bool numOfTrainers = false;
    bool createdTrainers = false;
    std::ifstream file(configFilePath);
    //read input from file, enter the details into proper vectors.
    while (file){
        char line[256];
        file.getline(line, 256);
        //jump to next line if comment/empty line
        if (line[0] == '#' || line[0] == '\0' )
            continue;
        //receive number of trainers;
        else if (!numOfTrainers){
            numOfTrainers = true;
            continue;
        }
        //read trainers capacity, create trainers that have such capacity.
        else if (!createdTrainers){
            int index = 0;
            while (line[index] != '\0'){
                //need to correct inorder to recieve a larger capacity than 9.
                string capacity;
                while (line[index] <='9' && line[index] >='0'){
                    capacity +=line[index];
                    index++;
                }
                //is this right way to create the trainer (memory wise)?
                int iCapacity = stoi(capacity);
                Trainer *temp = new Trainer(iCapacity);
                trainers.push_back(temp);
                if (line[index] != '\0')
                    index++;
            }
            createdTrainers = true;
            //read workout options, create workout that have such qualities.
        } else {
            int index = 0;
            string workoutName = "";
            WorkoutType w_type;
            string price = "";
            //get Workout name;
            while (line[index] != ','){
                workoutName +=line[index];
                index++;
            }
            index+=2;
            //get proper workout type
            if (line[index] =='A'){
                w_type = ANAEROBIC;
                index+=11;
            } else if (line[index] == 'C'){
                w_type = CARDIO;
                index+=8;
            } else {
                w_type = MIXED;
                index+=7;
            }
            //get price
            while (line[index] != '\0'){
                price +=line[index];
                index++;
            }
            int stoiPrice = stoi(price);
            //might be a problem to create workout so carelessly****** maybe need to initialize in heap.
            Workout temp(workout_options.size(),workoutName, stoiPrice,w_type);
            workout_options.push_back(temp);
        }
    }
}
void Studio::start() {
    cout << "Studio is now open!\n";
    string action;
    getline(cin, action);
    int customerIndex = 0;
    while (action != "closeall"){
        //order or open
        if (action[0] == 'o'){
            if (action[1]=='p'){
               openTrainer(action, customerIndex);
            } else {
                order(action);
            }
            //close or closeall
        } else if (action[0]=='c'){
            if (action[5] == 'a'){
                BaseAction* temp = new CloseAll();
                temp->act(*this);
                actionsLog.push_back(temp);
            }else{
                close(action);
            }
        //movecustomer
        } else if (action[0]=='m'){
            moveCustomer(action);
        //workout_options
        } else if (action[0]=='w'){
            BaseAction *temp = new PrintWorkoutOptions();
            temp->act(*this);
            actionsLog.push_back(temp);
        //status
        } else if (action[0]=='s'){
            status(action);
        //log
        } else if (action[0]=='l'){
            BaseAction *temp = new PrintActionsLog();
            temp->act(*this);
            actionsLog.push_back(temp);
         //backup
        } else if (action[0]=='b'){
            BaseAction *temp = new BackupStudio();
            temp->act(*this);
            actionsLog.push_back(temp);
        //restore
        } else if (action[0]=='r'){
            BaseAction *temp = new RestoreStudio();
            temp->act(*this);
            actionsLog.push_back(temp);
        }
        getline(cin, action);
    }
    BaseAction* closeFunc = new CloseAll();
    closeFunc->act(*this);
    actionsLog.push_back(closeFunc);
    clear();
}
void Studio::openTrainer(string input, int &index){
    string tempName;
    string tempType;
    string trainerId;
    int charIter = 5;
    while (input[charIter] >= '0' && input[charIter]<='9'){
        trainerId+=input[charIter];
        charIter++;
    }
    int stoiId = stoi(trainerId);
    int customerCounter = 0;
    vector<Customer * > customers;
    Trainer *trainer;
    while (input[charIter] != '\0'){
        tempName = "";
        tempType = "";
        if(input[charIter] == ' ')
            charIter++;
        //get name
        while (input[charIter] != ','){
            tempName+=input[charIter];
            charIter++;
        }
        charIter++;
        //get type
        while (input[charIter] != ' ' && input[charIter] !='\0'){
            tempType += input[charIter];
            charIter++;
        }
        trainer = this->getTrainer(stoiId);
        Customer *temp;
        if((stoiId >= 0) && (stoiId < getNumOfTrainers())) {
            if (trainer->getCapacity() - customerCounter > 0 && !trainer->isOpen()) {
                if (tempType == "swt")
                    temp = new SweatyCustomer(tempName, index);
                else if (tempType == "chp")
                    temp = new CheapCustomer(tempName, index);
                else if (tempType == "mcl")
                    temp = new HeavyMuscleCustomer(tempName, index);
                else
                    temp = new FullBodyCustomer(tempName, index);
                customers.push_back(temp);
                index++;
                customerCounter++;
            }
        }
    }
    BaseAction *temp = new OpenTrainer(stoiId, customers);
    temp->act(*this);
    actionsLog.push_back(temp);
}
void Studio::order (string input){
    int index = 6;
    string trainerId;
    while (input[index] >= '0' && input[index]<='9'){
        trainerId+=input[index];
        index++;
    }
    int stoiId = stoi(trainerId);
    BaseAction *temp = new Order(stoiId);
    temp->act(*this);
    actionsLog.push_back(temp);
}
void Studio::close(string input){
    int index = 6;
    string trainerId;
    while (input[index] >= '0' && input[index]<='9'){
        trainerId+=input[index];
        index++;
    }
    int stoiId = stoi(trainerId);
    BaseAction *temp = new Close(stoiId);
    temp->act(*this);
    actionsLog.push_back(temp);
}
void Studio::moveCustomer(std::string input) {
    int index = 5;
    string src;
    while (input[index] >= '0' && input[index]<='9'){
        src+=input[index];
        index++;
    }
    index++;
    string dest;
    while (input[index] >= '0' && input[index]<='9'){
        dest+=input[index];
        index++;
    }
    index++;
    string customer;
    while (input[index] >= '0' && input[index]<='9'){
        customer+=input[index];
        index++;
    }
    int stoiSrc = stoi(src);
    int stoiDest = stoi(dest);
    int stoiCustomer = stoi(customer);

    BaseAction *temp = new MoveCustomer(stoiSrc, stoiDest, stoiCustomer);
    temp->act(*this);
    actionsLog.push_back(temp);
}
void Studio::status(std::string input) {
    int index = 7;
    char trainer = 'c';
    int trainerId = 0;
    while (input[index] >= '0' && input[index] <='9'){
        trainerId = trainerId*10;
        trainer = input[index];
        trainerId += (trainer - '0');
        index++;
    }
    BaseAction *temp = new PrintTrainerStatus(trainerId);
    temp->act(*this);
    actionsLog.push_back(temp);
}
int Studio::getNumOfTrainers() const{
    return trainers.size();
}

vector<Trainer*> Studio::getTrainers (){
    return trainers;
}
Trainer* Studio::getTrainer(int tid){
    return trainers[tid];
}
const vector<BaseAction*>& Studio::getActionsLog() const{
    return actionsLog;
}
std::vector<Workout>& Studio::getWorkoutOptions(){
    return workout_options;
}

Studio::~Studio() {
    clear();

}
void Studio::clear(){
    for (Trainer *trainer: trainers){
        delete trainer;
    }
    trainers.clear();

    for (BaseAction *action: actionsLog){
        delete action;
    }
    actionsLog.clear();
    workout_options.clear();
}
//basically Studio creates a new object, and then we assign copy of Other.values to this object
Studio::Studio(const Studio &other) {
    open = other.open;
    //deep copy the trainers from other.
    for (Trainer *trainer :other.trainers){
        trainers.push_back(new Trainer(*trainer));
    }
    for (Workout oWorkout: other.workout_options){
        workout_options.push_back(Workout(oWorkout));
    }
    for (BaseAction *oAction :other.actionsLog){
        actionsLog.push_back(new Clone(oAction->getLog()));
    }
}

//clears this objects data, copys the data from other to this object.
Studio &Studio::operator=(const Studio &other) {
    if (this == &other)
        return *this;
    clear();
    open = other.open;
    //deep copy the trainers from other.
    for (Trainer *trainer :other.trainers){
        trainers.push_back(new Trainer(*trainer));
    }
    for (Workout oWorkout: other.workout_options){
        workout_options.push_back(Workout(oWorkout));
    }
    for (BaseAction *oAction :other.actionsLog){
        actionsLog.push_back(new Clone(oAction->getLog()));
    }
    return *this;
}

Studio::Studio(Studio &&other) {
    open = other.open;
    trainers = move(other.trainers);
    actionsLog = move(other.actionsLog);
    workout_options = move(other.workout_options);
    other.clear();
}
Studio &Studio::operator=(Studio &&other) {
    if (this == &other)
        return *this;
    else {
        clear();
        open = other.open;
        trainers = move(other.trainers);
        actionsLog = move(other.actionsLog);
        workout_options = move(other.workout_options);
        other.clear();
        return *this;
    }
}







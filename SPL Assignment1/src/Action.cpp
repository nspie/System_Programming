#include <string>
#include <iostream>
#include "Action.h"
#include "Studio.h"

using namespace std;

class Studio;

extern Studio *backup;


//baseAction
BaseAction::BaseAction() {
};

ActionStatus BaseAction::getStatus() const {
    return status;
}

void BaseAction::complete() {
    status = COMPLETED;
}

void BaseAction::error(string errormsg) {
    status = ERROR;
    this->errorMsg = errormsg;
}

string BaseAction::getErrorMsg() const {
    return errorMsg;
}

void BaseAction::setLog() {
    if (getStatus() == COMPLETED) {
        log = toString() + " " + "completed\n";
    } else {
        log = toString() + " " + "Error: " + getErrorMsg();
    }
}

string BaseAction::getLog() {
    return log;
}

BaseAction::~BaseAction() = default;

//openTrainer
OpenTrainer::OpenTrainer(int id, vector<Customer *> &customersList)
        : BaseAction(), trainerId(id), customers(move(customersList)) {
}

void OpenTrainer::act(Studio &studio) {
    //error : trainer does not exist
    if (((studio.getNumOfTrainers() <= trainerId) | (trainerId < 0))) {
        error("Trainer does not exist or is already open\n");
        cout << getErrorMsg();
    } else {
        Trainer *trainer = studio.getTrainer(trainerId);
        //error : trainer is already open
        if (trainer->isOpen()) {
            error("Trainer does not exist or is already open\n");
            cout << getErrorMsg();
        }
            //success
        else {
            trainer->openTrainer();
            int index = trainer->getCapacity();
            for (Customer *customer: customers) {
                trainer->addCustomer(customer);
                index--;
                if (index == 0)
                    break;
            }
            complete();
            //Check for empty customersList
            if (trainer->needToClose()) {
                trainer->closeTrainer();
                return;
            }
        }
    }
    setLog();
}

string OpenTrainer::toString() const {
    string output = "open " + to_string(trainerId) + " ";
    for (Customer *curr: customers) {
        output += curr->toString() + " ";
    }

    //deletes the last unnecessary " "
    output = output.substr(0, output.size() - 1);
    return output;
}

//Order
Order::Order(int id) : BaseAction(), trainerId(id) {
}

void Order::act(Studio &studio) {
    //error: trainer does not exist
    if (((studio.getNumOfTrainers() <= trainerId) | (trainerId < 0))) {
        error("Trainer does not exist or is not open\n");
        cout << getErrorMsg();
    } else {
        trainer = studio.getTrainer(trainerId);
        //error : trainer is not open
        if (!(trainer->isOpen())) {
            error("Trainer does not exist or is not open\n");
            cout << getErrorMsg();
        }
            //success
        else {
            trainer->multiOrder(studio.getWorkoutOptions());
            string output = "";
            for (OrderPair pair: trainer->getOrders()) {
                output = trainer->getCustomer(pair.first)->getName() + " Is Doing " + pair.second.getName() + "\n";
                cout << output;
            }
            complete();
        }
    }
    setLog();
}

string Order::toString() const {
    return "order " + to_string(trainerId);
}


//move customer
MoveCustomer::MoveCustomer(int src, int dst, int customerId)
        : BaseAction(), srcTrainer(src), dstTrainer(dst), id(customerId) {

}

void MoveCustomer::act(Studio &studio) {
    //error : trainers do not exist
    if (((studio.getNumOfTrainers() <= srcTrainer) | (srcTrainer < 0)) ||
        ((studio.getNumOfTrainers() <= dstTrainer) | (dstTrainer < 0))) {
        error("Cannot move customer\n");
        cout << getErrorMsg();
    } else {
        Trainer *source = studio.getTrainer(srcTrainer);
        Trainer *destination = studio.getTrainer(dstTrainer);
        //error : trainers or destination trainer is fully booked.
        if (!(source->isOpen()) || !(destination->isOpen()) || !(destination->canReceive())) {
            error("Cannot move customer\n");
            cout << getErrorMsg();
        } else {
            Customer *movingCustomer = source->getCustomer(id);
            //error:customer does not exist
            if (movingCustomer == nullptr) {
                error("Cannot move customer\n");
                cout << getErrorMsg();
            }
                //success - execute move customer
            else {
                destination->addCustomer(movingCustomer);
                source->removeCustomer(id);
                if (movingCustomer->getOrdered()) {
                    destination->order(id, movingCustomer->order(studio.getWorkoutOptions()),
                                       studio.getWorkoutOptions());
                }
                complete();
            }
        }
    }
    setLog();
}


string MoveCustomer::toString() const {
    return "move " + to_string(srcTrainer) + " " + to_string(dstTrainer) + " " + to_string(id);
}


//Close
Close::Close(int id) : BaseAction(), trainerId(id) {
}

void Close::act(Studio &studio) {
    //error : trainer does not exist
    if (((studio.getNumOfTrainers() <= trainerId) | (trainerId < 0))) {
        error("Trainer does not exist or is not open.\n");
        cout << getErrorMsg();
    } else {
        Trainer *trainer = studio.getTrainer(trainerId);
        //error : trainer is not open
        if (!trainer->isOpen()) {
            error("Trainer does not exist or is not open.\n");
            cout << getErrorMsg();
        }
            //success
        else {
            string output = "";
           output =  "Trainer " + to_string(trainerId) + " closed. Salary " + to_string(trainer->getSalary()) + "NIS\n";
            cout << output;
            trainer->closeTrainer();
            complete();
        }
    }
    setLog();
}

string Close::toString() const {
    return "close " + to_string(trainerId);
}


//closeAll
CloseAll::CloseAll() : BaseAction() {
}

void CloseAll::act(Studio &studio) {
    int location = 0;
    string output = "";
    for (Trainer *trainer: studio.getTrainers()) {
        if (trainer->isOpen()) {
            trainer->closeTrainer();
            string loc = to_string(location);
            string sal = to_string(trainer->getSalary());
            output = "Trainer " + loc + " closed. " + "Salary " + sal +"NIS\n";
            cout << output;
        }
        location += 1;
    }
    complete();
    setLog();
}


string CloseAll::toString() const {
    return "closeall";
}


//printWorkoutOption
PrintWorkoutOptions::PrintWorkoutOptions() : BaseAction() {
}

void PrintWorkoutOptions::act(Studio &studio) {
    for (Workout workout: studio.getWorkoutOptions()) {
        cout << workout.getName() + ", " + to_string(workout.getType()) + ", " + to_string(workout.getPrice()) + "\n";
    }
    complete();
    setLog();
}

string PrintWorkoutOptions::toString() const {
    return "workout_options";
}


//PrintTrainerStatus
PrintTrainerStatus::PrintTrainerStatus(int id) : BaseAction(), trainerId(id) {
}

void PrintTrainerStatus::act(Studio &studio) {
    if (trainerId < 0 || trainerId >= studio.getNumOfTrainers())
        error("");
    else {
        Trainer *trainer = studio.getTrainer(trainerId);
        string output = "";
        if (trainer->isOpen()) {
            output = "Trainer " + to_string(trainerId) + " status: " + "open" + "\n";
            cout << output;
            cout << "Customers:\n";
            for (Customer *customer: trainer->getCustomers()) {
                cout << to_string(customer->getId()) + " " + customer->getName() + "\n";
            }
            cout << "Orders:\n";
            for (const OrderPair &pair: trainer->getOrders()) {
                output = pair.second.getName() + " " + to_string(pair.second.getPrice()) + "NIS " +
                         to_string(pair.first) +
                         "\n";
                cout << output;
            }
            output = "Current Trainer's Salary: " + to_string(trainer->getSalary()) + "NIS\n";
            cout << output;
        } else {
            output = "Trainer " + to_string(trainerId) + " status: " + "closed\n";
            cout << output;
        }
        complete();
        setLog();
    }
}

string PrintTrainerStatus::toString() const {
    if (getStatus() == ERROR)
        return getErrorMsg();
    return "status " + to_string(trainerId);
}


//PrintActionsLog

PrintActionsLog::PrintActionsLog() : BaseAction() {

}

void PrintActionsLog::act(Studio &studio) {
    for (BaseAction *baseAction: studio.getActionsLog()) {
        cout << baseAction->getLog();
    }
    complete();
}

string PrintActionsLog::toString() const {
    return "log";
}


//BackupStudio

BackupStudio::BackupStudio() : BaseAction() {
}

void BackupStudio::act(Studio &studio) {
    if (backup != nullptr) {
        delete backup;
    }
    backup = new Studio(studio);
    complete();
    setLog();
}

string BackupStudio::toString() const {
    return "backup";
}

//RestoreStudio
RestoreStudio::RestoreStudio() : BaseAction() {
}

void RestoreStudio::act(Studio &studio) {
    if (backup == nullptr) {
        error("No backup available\n");
        cout << getErrorMsg();
    } else {
        studio = *backup;
        complete();
    }
    setLog();
}

string RestoreStudio::toString() const {
    return "restore";
}

//Clone

Clone::Clone(string logc) : BaseAction() {
    log = logc;
}

void Clone::act(Studio &studio) {

}

string Clone::toString() const {
    return "";
}

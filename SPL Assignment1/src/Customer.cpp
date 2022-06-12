#include "Customer.h"
#include <iostream>
#include <algorithm>
using namespace std;

Customer::Customer(string c_name, int c_id) : name(c_name), id(c_id), isOrdered(false) {
}
string Customer::getName() const {
    return name;
}
int Customer::getId() const {
    return id;
}

Customer::~Customer() = default;

bool Customer::getOrdered() {
    return isOrdered;
}

void Customer::setOrdered(bool status) {
    isOrdered = status;
}

//sweatyCustomer
SweatyCustomer::SweatyCustomer(std::string name, int id): Customer(name, id){
}
vector<int> SweatyCustomer::order(const vector<Workout> &workout_options){
    vector<int> output;
    for (Workout curr: workout_options)
        if (curr.getType() == CARDIO)
            output.push_back(curr.getId());
    return output;
}
string SweatyCustomer::toString() const {
    return getName() + ",swt";
}

SweatyCustomer* SweatyCustomer::clone(){
    SweatyCustomer *temp = new SweatyCustomer(getName(), getId());
    temp->setOrdered(getOrdered());
    return temp;
}

//CheapCustomer
CheapCustomer::CheapCustomer(std::string name, int id): Customer(name, id){
}
vector<int> CheapCustomer::order(const vector<Workout> &workout_options){
    vector<int> output;
    int minId;
    int minPrice;
    bool assigned = false;
    for (Workout curr: workout_options) {
        if (!assigned) {
            minId = curr.getId();
            minPrice = curr.getPrice();
            assigned = true;
        } else if (minPrice > curr.getPrice()) {
            minId = curr.getId();
            minPrice = curr.getPrice();
        } else if (minPrice == curr.getPrice()){
            minId = min(minId, curr.getId());
        }
    }
    output.push_back(minId);
    return output;
}
string CheapCustomer::toString() const {
    return getName() + ",chp";
}

CheapCustomer* CheapCustomer::clone(){
    CheapCustomer *temp = new CheapCustomer(getName(), getId());
    temp->setOrdered(getOrdered());
    return temp;
}

//HeavyMuscleCustomer
HeavyMuscleCustomer::HeavyMuscleCustomer(std::string name, int id): Customer(name, id){
}

bool HeavyMuscleCustomer::compareWorkouts(const Workout lhs, const Workout rhs) {
    if (lhs.getPrice() == rhs.getPrice()){
        return lhs.getId() < lhs.getId();
    }
    else {
        return lhs.getPrice() > rhs.getPrice();
    }
}
vector<int> HeavyMuscleCustomer::order(const vector<Workout> &workout_options) {
    vector<int> output;
    for (Workout curr: workout_options)
        if (curr.getType() == ANAEROBIC)
            output.push_back(curr.getId());


        //sorting the output vector
    std::sort(output.begin(), output.end(), [&workout_options](const int &x, const int &y)->bool {
        return workout_options.at(x).getPrice() < workout_options.at(y).getPrice();});
    std::reverse(output.begin(), output.end());

    return output;
}
string HeavyMuscleCustomer::toString() const {
    return getName() + ",mcl";
}

HeavyMuscleCustomer* HeavyMuscleCustomer::clone(){
    HeavyMuscleCustomer *temp = new HeavyMuscleCustomer(getName(), getId());
    temp->setOrdered(getOrdered());
    return temp;
}

//FullBodyCustomer
FullBodyCustomer::FullBodyCustomer(std::string name, int id): Customer(name, id){
}
vector<int> FullBodyCustomer::order(const vector<Workout> &workout_options){
    int minCardioId = 2147483647;
    int minCardioPrice = 2147483647;
    int maxMixedId = -1;
    int maxMixedPrice = -1;
    int minAnaerobicId = 2147483647;
    int minAnaerobicPrice = 2147483647;
    bool Cassigned = false;
    bool Massigned = false;
    bool Aassigned = false;
    for (Workout curr: workout_options) {
        if (curr.getType() == CARDIO){
            if (!Cassigned) {
                minCardioId = curr.getId();
                minCardioPrice = curr.getPrice();
                Cassigned = true;
            } else if (minCardioPrice > curr.getPrice()) {
                minCardioId = curr.getId();
                minCardioPrice = curr.getPrice();
            } else if (minCardioPrice == curr.getPrice()){
                minCardioId = min(minCardioId, curr.getId());
            }
        } else if (curr.getType() == MIXED){
            if (!Massigned) {
                maxMixedId = curr.getId();
                maxMixedPrice = curr.getPrice();
                Massigned = true;
            } else if (maxMixedPrice < curr.getPrice()) {
                maxMixedId = curr.getId();
                maxMixedPrice = curr.getPrice();
            } else if (minCardioPrice == curr.getPrice()){
                maxMixedId = min(maxMixedId, curr.getId());
            }
        } else {
            if (!Aassigned) {
                minAnaerobicId = curr.getId();
                minAnaerobicPrice = curr.getPrice();
                Aassigned = true;
            } else if (minAnaerobicPrice > curr.getPrice()) {
                minAnaerobicId = curr.getId();
                minAnaerobicPrice = curr.getPrice();
            } else if (minCardioPrice == curr.getPrice()){
                minAnaerobicId = min(minAnaerobicId, curr.getId());
            }
        }

    }
    vector<int> output;
    //found good Cardio workout
    if (Cassigned)
        output.push_back(minCardioId);
    //found good Mixed workout
    if (Massigned)
        output.push_back(maxMixedId);
    //found good Anaerobic workout
    if (Aassigned)
        output.push_back(minAnaerobicId);
    return output;
}
string FullBodyCustomer::toString() const {
    return getName() + ",fbd";
}

FullBodyCustomer* FullBodyCustomer::clone(){
    FullBodyCustomer *temp = new FullBodyCustomer(getName(), getId());
    temp->setOrdered(getOrdered());
    return temp;
}
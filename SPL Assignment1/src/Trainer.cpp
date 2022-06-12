#include "Trainer.h"
#include <iostream>

using namespace std;

//do we need to explicitly initialize the customerList and orderList vectors
Trainer::Trainer(int t_capacity) : capacity(t_capacity), open(false) {
    salary = 0;
}

void Trainer::clear() {
    for (Customer *curr: customersList){
        if (curr != nullptr){
            delete curr;
        }
    }
    customersList.clear();
    orderList.clear();
}

//destructor
Trainer::~Trainer() {
    clear();
}

//Copy constructor
Trainer::Trainer(const Trainer &other) {
    capacity = other.getCapacity();
    open = other.open;
    salary = other.salary;
    for(Customer *customer : other.customersList){
        this->customersList.push_back(customer->clone());
    }
    for (OrderPair pair : other.orderList){
        this->orderList.push_back(pair);
    }
}

//Copy operator
Trainer &Trainer::operator=(const Trainer &other) {
    if (this == &other)
        return *this;
    clear();
    capacity = other.capacity;
    open = other.open;
    salary = other.salary;

    for (Customer *customer: other.customersList) {
        this->customersList.push_back(customer->clone());
    }
    for (OrderPair pair : other.orderList){
        this->orderList.push_back(pair);
    }
    return *this;
}

//Move constructor
Trainer::Trainer(Trainer &&other) : capacity(other.getCapacity()), open(other.open), salary(other.salary), customersList(move(other.customersList)), orderList(move(other.orderList)) {
    other.clear();
}

//= operator
Trainer &Trainer::operator=(Trainer &&other) {
    if (this == &other)
        return *this;
    else{
        clear();
        capacity = other.getCapacity();
        open = other.open;
        salary = other.salary;
        customersList = move(other.customersList);
        orderList = move(other.orderList);
        other.clear();
        return *this;
    }
}


int Trainer::getCapacity() const {
    return capacity;
}

void Trainer::addCustomer(Customer *customer) {
    customersList.push_back(customer);
}

void Trainer::removeCustomer(int id) {
    bool found = false;
    int index = 0;
    for (Customer *curr: customersList) {
        if (curr->getId() == id) {
            found = true;
            break;
        } else
            index++;
    }

    //Clear the customerList vector
    if (found) {
        customersList.erase(customersList.begin() + index);//+-1 during erase  //does erase doing good job?
    }

    //Check for empty customersList
    if (needToClose()){
        closeTrainer();
        return;
    }

    //Clear the orderList vector - dummy clear to avoid warnings
    vector<OrderPair> temp;
    for (OrderPair pair: orderList) {
        if (pair.first != id) {
            temp.push_back(pair);
        }
    }
    orderList.swap(temp);
    temp.clear();
}

Customer *Trainer::getCustomer(int id) {
    for (Customer *curr: customersList) {
        if (curr->getId() == id)
            return curr;
    }
    return nullptr;
}

vector<Customer *> &Trainer::getCustomers() {
    //might be a problem. check during runtime if correct return.
    return customersList;
}

vector<OrderPair> &Trainer::getOrders() {
    //might be a problem. check during runtime if correct return.
    return orderList;
}

void Trainer::order(const int customer_id, const std::vector<int> workout_ids, const std::vector<Workout> &workout_options) {
    //this is correct under assumption that every customer has a strategy that he can execute from workoutOptions.
    for (int workoutId: workout_ids) {
        OrderPair temp(customer_id, workout_options[workoutId]);
        orderList.push_back(temp);
    }
}


void Trainer::openTrainer() {
    if (!open) {
        open = true;
    }
}

void Trainer::closeTrainer() {
    if (open) {
        open = false;
        salary = getSalary();
        clear();
    }
}

int Trainer::getSalary() {
    int sum = 0;
    for (const OrderPair &curr: orderList) {
        sum += (curr.second.getPrice());
    }
    return salary + sum;
}

bool Trainer::canReceive(){
    size_t cap = capacity;
    return cap > customersList.size();
}

bool Trainer::needToClose(){
    bool output = false;
    if(customersList.empty()){
        closeTrainer();
        output = true;
    }
    return output;
}



bool Trainer::isOpen() {
    return open;
}

void Trainer::multiOrder(const std::vector<Workout> &workout_options) {
    for (Customer *curr: customersList) {
        if (! curr->getOrdered()) {
            order(curr->getId(), curr->order(workout_options), workout_options);
            curr->setOrdered(true);
        }
    }
}

//to be fulfilled.
string Trainer::toString() {
    return "";

}

#ifndef TRAINER_H_
#define TRAINER_H_

#include <vector>
#include "Customer.h"
#include "Workout.h"

typedef std::pair<int, Workout> OrderPair;

class Trainer{
public:
    Trainer(int t_capacity);
    //clears the trainer from all data
    void clear();
    //destructor
    virtual ~Trainer();
    //copy constructor
    Trainer (const Trainer &other);
    //copy operator
    Trainer & operator=(const Trainer &other);
    //move constructor
    Trainer (Trainer &&other);
    //move operator
    Trainer & operator=(Trainer &&other);


    int getCapacity() const;
    void addCustomer(Customer* customer);
    void removeCustomer(int id);
    Customer* getCustomer(int id);
    std::vector<Customer*>& getCustomers();
    std::vector<OrderPair>& getOrders();
    void order(const int customer_id, const std::vector<int> workout_ids, const std::vector<Workout>& workout_options);
    void openTrainer();
    void closeTrainer();
    int getSalary();
    bool canReceive();
    bool needToClose();
    bool isOpen();
    void multiOrder(const std::vector<Workout>& workout_options);
    std::string toString();

private:
    int capacity;
    bool open;
    int salary; //updates after a workout session is closed
    std::vector<Customer*> customersList;
    std::vector<OrderPair> orderList; //A list of pairs for each order for the trainer - (customer_id, Workout)

};


#endif
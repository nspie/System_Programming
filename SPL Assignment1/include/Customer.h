#ifndef CUSTOMER_H_
#define CUSTOMER_H_

#include <vector>
#include <string>
#include "Workout.h"


class Customer {
public:
    //virtual ~Customer() = default;
    Customer(std::string c_name, int c_id);

    virtual std::vector<int> order(const std::vector<Workout> &workout_options) = 0;

    virtual std::string toString() const = 0;

    virtual Customer *clone() = 0;

    std::string getName() const;

    virtual ~Customer();

    int getId() const;


    bool getOrdered();

    void setOrdered(bool status);

private:
    const std::string name;
    const int id;
    bool isOrdered;

};


class SweatyCustomer : public Customer {
public:
    SweatyCustomer(std::string name, int id);

    std::vector<int> order(const std::vector<Workout> &workout_options);

    std::string toString() const;

    SweatyCustomer *clone();


private:
};


class CheapCustomer : public Customer {
public:
    CheapCustomer(std::string name, int id);

    std::vector<int> order(const std::vector<Workout> &workout_options);

    std::string toString() const;

    CheapCustomer *clone();

private:
};


class HeavyMuscleCustomer : public Customer {
public:
    HeavyMuscleCustomer(std::string name, int id);

    static bool compareWorkouts(const Workout lhs, const Workout rhs);

    std::vector<int> order(const std::vector<Workout> &workout_options);

    std::string toString() const;

    HeavyMuscleCustomer *clone();

private:
};


class FullBodyCustomer : public Customer {
public:
    FullBodyCustomer(std::string name, int id);

    std::vector<int> order(const std::vector<Workout> &workout_options);

    std::string toString() const;

    FullBodyCustomer *clone();

private:
};


#endif
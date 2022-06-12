#ifndef STUDIO_H_
#define STUDIO_H_

#include <vector>
#include <string>
#include "Workout.h"
#include "Trainer.h"
#include "Action.h"

class BaseAction;

class Studio{
public:
    Studio();
    Studio(const std::string &configFilePath);
    void start();
    void openTrainer (std::string input, int &index);
    void order (std::string input);
    void close (std::string input);
    void moveCustomer(std::string input);
    void status(std::string input);
    int getNumOfTrainers() const;
    Trainer* getTrainer(int tid);
    std::vector<Trainer*> getTrainers();
    const std::vector<BaseAction*>& getActionsLog() const; // Return a reference to the history of actions
    std::vector<Workout>& getWorkoutOptions();
    //destructor
    virtual ~Studio();
    void clear();
    //copy constructor
    Studio (const Studio &other);
    //copy operator
    Studio & operator=(const Studio &other);
    //move constructor
    Studio (Studio &&other);
    //move operator
    Studio & operator=(Studio &&other);
private:
    bool open;
    std::vector<Trainer*> trainers;
    std::vector<Workout> workout_options;
    std::vector<BaseAction*> actionsLog;
};

#endif
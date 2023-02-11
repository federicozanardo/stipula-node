package vm.event;

import models.dto.requests.event.EventSchedulingRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.locks.ReentrantLock;

public class EventScheduler {
    private final ArrayList<EventTrigger> tasks;
    private final Timer timer;
    private final ReentrantLock mutex;

    public EventScheduler() {
        this.tasks = new ArrayList<>();
        this.timer = new Timer();
        this.mutex = new ReentrantLock();
    }

    /**
     * This method allows to schedule a new task.
     *
     * @param task: task to be scheduled.
     */
    public void addTask(EventTrigger task) {
        mutex.lock();

        timer.schedule(task, new Date(task.getSchedulingRequest().getRequest().getTime() * 1000L));
        tasks.add(task);

        mutex.unlock();
    }

    /**
     * This method allows to remove a scheduled tasks from the list.
     *
     * @param schedulingRequest: the request to be removed.
     */
    public void removeTask(EventSchedulingRequest schedulingRequest) {
        mutex.lock();

        int i = 0;
        boolean found = false;

        while (i < tasks.size() && !found) {
            EventTrigger task = tasks.get(i);
            EventSchedulingRequest taskRequest = task.getSchedulingRequest();

            if (taskRequest.getContractId().equals(schedulingRequest.getContractId()) &&
                    taskRequest.getContractInstanceId().equals(schedulingRequest.getContractInstanceId()) &&
                    taskRequest.getRequest().getObligationFunctionName().equals(schedulingRequest.getRequest().getObligationFunctionName())) {
                found = true;
            } else {
                i++;
            }
        }

        if (!found) {
            throw new Error();
        }

        tasks.remove(i);
        mutex.unlock();
    }

    /**
     * This method allows to get all the current scheduled tasks.
     *
     * @return the list of scheduled tasks.
     */
    public ArrayList<EventTrigger> getTasks() {
        return tasks;
    }

    @Override
    public String toString() {
        return "EventScheduler{" +
                "tasks=" + tasks +
                '}';
    }
}

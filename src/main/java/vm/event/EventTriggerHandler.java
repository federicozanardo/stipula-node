package vm.event;

import models.dto.requests.event.EventTriggerSchedulingRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.locks.ReentrantLock;

public class EventTriggerHandler {
    private final ArrayList<EventTrigger> tasks;
    private final Timer timer;
    private final ReentrantLock mutex;

    public EventTriggerHandler() {
        this.tasks = new ArrayList<>();
        this.timer = new Timer();
        this.mutex = new ReentrantLock();
    }

    public void addTask(EventTrigger task) {
        mutex.lock();

        // timer.schedule(task, task.getSchedulingRequest().getRequest().getTime() * 1000L);
        timer.schedule(task, new Date(task.getSchedulingRequest().getRequest().getTime() * 1000L));
        tasks.add(task);

        mutex.unlock();
    }

    public void removeTask(EventTriggerSchedulingRequest schedulingRequest) {
        mutex.lock();

        int i = 0;
        boolean found = false;

        while (i < tasks.size() && !found) {
            EventTrigger task = tasks.get(i);
            EventTriggerSchedulingRequest taskRequest = task.getSchedulingRequest();

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

    public ArrayList<EventTrigger> getTasks() {
        return tasks;
    }
}

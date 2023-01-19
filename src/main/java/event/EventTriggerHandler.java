package event;

import models.dto.requests.event.EventTriggerRequest;
import models.dto.requests.event.EventTriggerSchedulingRequest;
import vm.RequestQueue;
import vm.VirtualMachine;

import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.locks.ReentrantLock;

public class EventTriggerHandler {
    private final ArrayList<EventTrigger> tasks;
    private final Timer timer;
    private final RequestQueue requestQueue;
    private final VirtualMachine virtualMachine;
    private final ReentrantLock mutex;

    public EventTriggerHandler(RequestQueue requestQueue, VirtualMachine virtualMachine) {
        this.requestQueue = requestQueue;
        this.virtualMachine = virtualMachine;
        this.tasks = new ArrayList<>();
        this.timer = new Timer();
        this.mutex = new ReentrantLock();
    }

    public void addTask(EventTriggerSchedulingRequest schedulingRequest) {
        EventTrigger task = new EventTrigger(schedulingRequest, this, requestQueue, virtualMachine);
        this.mutex.lock();

        this.timer.schedule(task, schedulingRequest.getSecondsBeforeCalling() * 1000L);
        this.tasks.add(task);

        this.mutex.unlock();
    }

    public void removeTask(EventTriggerRequest request) {
        this.mutex.lock();

        int i = 0;
        boolean found = false;

        while (i < tasks.size() && !found) {
            EventTrigger task = tasks.get(i);
            EventTriggerRequest taskRequest = task.getSchedulingRequest().getRequest();

            if (taskRequest.getContractId().equals(request.getContractId()) &&
                    taskRequest.getContractInstanceId().equals(request.getContractInstanceId()) &&
                    taskRequest.getObligationName().equals(request.getObligationName())) {
                found = true;
            } else {
                i++;
            }
        }

        if (!found) {
            throw new Error();
        }

        this.tasks.remove(i);
        this.mutex.unlock();
    }

    public ArrayList<EventTrigger> getTasks() {
        return tasks;
    }
}

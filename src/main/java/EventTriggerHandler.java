import lib.datastructures.RequestQueue;
import models.dto.requests.EventTriggerRequest;
import models.dto.requests.EventTriggerSchedulingRequest;

import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.locks.ReentrantLock;

public class EventTriggerHandler {
    private final ArrayList<EventTriggerTask> tasks;
    private final Timer timer;
    private final RequestQueue requestQueue;
    private final ReentrantLock mutex;

    public EventTriggerHandler(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
        this.tasks = new ArrayList<>();
        this.timer = new Timer();
        this.mutex = new ReentrantLock();
    }

    public void addTask(EventTriggerSchedulingRequest schedulingRequest) {
        EventTriggerTask task = new EventTriggerTask(schedulingRequest.getRequest(), this, requestQueue);
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
            EventTriggerTask task = tasks.get(i);
            if (task.getRequest().getContractId().equals(request.getContractId()) &&
                    task.getRequest().getContractInstanceId().equals(request.getContractInstanceId()) &&
                    task.getRequest().getTriggerName().equals(request.getTriggerName())) {
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

    public ArrayList<EventTriggerTask> getTasks() {
        return tasks;
    }
}

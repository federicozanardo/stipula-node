package vm.event;

import exceptions.datastructures.queue.QueueOverflowException;
import models.dto.requests.event.EventSchedulingRequest;
import vm.RequestQueue;

import java.util.TimerTask;

public class EventTrigger extends TimerTask {
    private final EventSchedulingRequest schedulingRequest;
    private final EventScheduler scheduler;
    private final RequestQueue requestQueue;
    private final Thread virtualMachineThread;

    public EventTrigger(
            EventSchedulingRequest schedulingRequest,
            EventScheduler scheduler,
            RequestQueue requestQueue,
            Thread virtualMachineThread
    ) {
        this.schedulingRequest = schedulingRequest;
        this.scheduler = scheduler;
        this.requestQueue = requestQueue;
        this.virtualMachineThread = virtualMachineThread;
    }

    @Override
    public void run() {
        System.out.println("EventTrigger: A new scheduled request has been triggered => " + this.schedulingRequest);
        System.out.println("EventTrigger: Enqueuing the request...");
        try {
            this.requestQueue.enqueue(schedulingRequest);
        } catch (QueueOverflowException e) {
            throw new RuntimeException(e);
        }

        System.out.println("EventTrigger: Notifying the virtual machine...");
        synchronized (this.virtualMachineThread) {
            this.virtualMachineThread.notify();
        }
        System.out.println("EventTrigger: Virtual machine notified");

        System.out.println("EventTrigger: Removing the request from EventTriggerHandler...");
        this.scheduler.removeTask(schedulingRequest);
    }

    public EventSchedulingRequest getSchedulingRequest() {
        return schedulingRequest;
    }

    @Override
    public String toString() {
        return "EventTrigger{" +
                "schedulingRequest=" + schedulingRequest +
                ", scheduler=" + scheduler +
                ", requestQueue=" + requestQueue +
                ", virtualMachineThread=" + virtualMachineThread +
                '}';
    }
}

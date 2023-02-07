package vm.event;

import exceptions.datastructures.queue.QueueOverflowException;
import models.dto.requests.event.EventTriggerSchedulingRequest;
import vm.RequestQueue;

import java.util.TimerTask;

public class EventTrigger extends TimerTask {
    private final EventTriggerSchedulingRequest schedulingRequest;
    private final EventTriggerHandler handler;
    private final RequestQueue requestQueue;
    private final Thread virtualMachineThread;

    public EventTrigger(
            EventTriggerSchedulingRequest schedulingRequest,
            EventTriggerHandler handler,
            RequestQueue requestQueue,
            Thread virtualMachineThread
    ) {
        this.schedulingRequest = schedulingRequest;
        this.handler = handler;
        this.requestQueue = requestQueue;
        this.virtualMachineThread = virtualMachineThread;
    }

    @Override
    public void run() {
        System.out.println("EventTrigger: request raised => " + this.schedulingRequest);
        System.out.println("EventTrigger: enqueue the request...");
        try {
            this.requestQueue.enqueue(schedulingRequest);
        } catch (QueueOverflowException e) {
            throw new RuntimeException(e);
        }

        System.out.println("EventTrigger: notifying the virtual machine...");
        synchronized (this.virtualMachineThread) {
            this.virtualMachineThread.notify();
        }
        System.out.println("EventTrigger: virtual machine notified");

        System.out.println("EventTrigger: removing the request from EventTriggerHandler...");
        this.handler.removeTask(schedulingRequest);
    }

    public EventTriggerSchedulingRequest getSchedulingRequest() {
        return schedulingRequest;
    }

    @Override
    public String toString() {
        return "EventTrigger{" +
                "schedulingRequest=" + schedulingRequest +
                ", handler=" + handler +
                ", requestQueue=" + requestQueue +
                '}';
    }
}

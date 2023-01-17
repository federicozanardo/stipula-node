package event;

import exceptions.queue.QueueOverflowException;
import lib.datastructures.RequestQueue;
import models.dto.requests.event.EventTriggerSchedulingRequest;

import java.util.TimerTask;

public class EventTrigger extends TimerTask {
    private final EventTriggerSchedulingRequest schedulingRequest;
    private final EventTriggerHandler handler;
    private final RequestQueue requestQueue;

    public EventTrigger(EventTriggerSchedulingRequest schedulingRequest, EventTriggerHandler handler, RequestQueue requestQueue) {
        this.schedulingRequest = schedulingRequest;
        this.handler = handler;
        this.requestQueue = requestQueue;
    }

    @Override
    public void run() {
        System.out.println("EventTriggerTask: request raised => " + this.schedulingRequest);
        System.out.println("EventTriggerTask: enqueue the request...");
        try {
            this.requestQueue.enqueue(schedulingRequest);
        } catch (QueueOverflowException e) {
            throw new RuntimeException(e);
        }
        System.out.println("EventTriggerTask: removing the request from EventTriggerHandler...");
        this.handler.removeTask(schedulingRequest.getRequest());
    }

    public EventTriggerSchedulingRequest getSchedulingRequest() {
        return schedulingRequest;
    }

    @Override
    public String toString() {
        return "EventTriggerTask{" +
                "schedulingRequest=" + schedulingRequest +
                ", handler=" + handler +
                ", requestQueue=" + requestQueue +
                '}';
    }
}

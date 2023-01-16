import exceptions.queue.QueueOverflowException;
import lib.datastructures.RequestQueue;
import models.dto.requests.event.EventTriggerRequest;

import java.util.TimerTask;

public class EventTriggerTask extends TimerTask {
    private final EventTriggerRequest request;
    private final EventTriggerHandler handler;
    private final RequestQueue requestQueue;

    public EventTriggerTask(EventTriggerRequest request, EventTriggerHandler handler, RequestQueue requestQueue) {
        this.request = request;
        this.handler = handler;
        this.requestQueue = requestQueue;
    }

    @Override
    public void run() {
        System.out.println("EventTriggerTask: request raised => " + this.request);
        System.out.println("EventTriggerTask: enqueue the request...");
        try {
            this.requestQueue.enqueue(request);
        } catch (QueueOverflowException e) {
            throw new RuntimeException(e);
        }
        System.out.println("EventTriggerTask: removing the request from EventTriggerHandler...");
        this.handler.removeTask(request);
    }

    public EventTriggerRequest getRequest() {
        return request;
    }

    @Override
    public String toString() {
        return "EventTriggerTask{" +
                "request=" + request +
                '}';
    }
}

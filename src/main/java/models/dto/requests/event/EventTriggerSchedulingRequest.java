package models.dto.requests.event;

import models.dto.requests.Message;

public class EventTriggerSchedulingRequest extends Message {
    private final EventTriggerRequest request;
    private final int secondsBeforeCalling;

    public EventTriggerSchedulingRequest(EventTriggerRequest request, int secondsBeforeCalling) {
        super(EventTriggerSchedulingRequest.class.getSimpleName());
        this.request = request;
        this.secondsBeforeCalling = secondsBeforeCalling;
    }

    public EventTriggerRequest getRequest() {
        return request;
    }

    public int getSecondsBeforeCalling() {
        return secondsBeforeCalling;
    }

    @Override
    public String toString() {
        return "EventTriggerSchedulingRequest{" +
                "request=" + request +
                ", secondsBeforeCalling=" + secondsBeforeCalling +
                '}';
    }
}

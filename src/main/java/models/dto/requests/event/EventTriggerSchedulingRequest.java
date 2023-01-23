package models.dto.requests.event;

public class EventTriggerSchedulingRequest {
    private final EventTriggerRequest request;
    private final int secondsBeforeCalling;

    public EventTriggerSchedulingRequest(EventTriggerRequest request, int secondsBeforeCalling) {
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

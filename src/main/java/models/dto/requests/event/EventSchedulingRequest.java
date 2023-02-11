package models.dto.requests.event;

public class EventSchedulingRequest {
    private final CreateEventRequest request;
    private final String contractId;
    private final String contractInstanceId;

    public EventSchedulingRequest(
            CreateEventRequest request,
            String contractId,
            String contractInstanceId
    ) {
        this.request = request;
        this.contractId = contractId;
        this.contractInstanceId = contractInstanceId;
    }

    public CreateEventRequest getRequest() {
        return request;
    }

    public String getContractId() {
        return contractId;
    }

    public String getContractInstanceId() {
        return contractInstanceId;
    }

    @Override
    public String toString() {
        return "EventTriggerSchedulingRequest{" +
                "request=" + request +
                ", contractId='" + contractId + '\'' +
                ", contractInstanceId='" + contractInstanceId + '\'' +
                '}';
    }
}

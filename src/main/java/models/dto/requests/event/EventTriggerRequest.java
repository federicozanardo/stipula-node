package models.dto.requests.event;

import models.dto.requests.Message;

public class EventTriggerRequest extends Message {
    private final String contractId;
    private final String contractInstanceId;
    private final String triggerName;

    public EventTriggerRequest(String contractId, String contractInstanceId, String triggerName) {
        super(EventTriggerRequest.class.getSimpleName());
        this.contractId = contractId;
        this.contractInstanceId = contractInstanceId;
        this.triggerName = triggerName;
    }

    public String getContractId() {
        return contractId;
    }

    public String getContractInstanceId() {
        return contractInstanceId;
    }

    public String getTriggerName() {
        return triggerName;
    }

    @Override
    public String toString() {
        return "EventTriggerRequest{" +
                "contractId='" + contractId + '\'' +
                ", contractInstanceId='" + contractInstanceId + '\'' +
                ", functionName='" + triggerName + '\'' +
                '}';
    }
}

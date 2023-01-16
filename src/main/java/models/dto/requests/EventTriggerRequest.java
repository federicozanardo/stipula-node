package models.dto.requests;

public class EventTriggerRequest extends Message {
    private final String contractId;
    private final String contractInstanceId;
    private final String functionName;
    public EventTriggerRequest(String contractId, String contractInstanceId, String triggerName) {
        super(EventTriggerRequest.class.getSimpleName());
        this.contractId = contractId;
        this.contractInstanceId = contractInstanceId;
        this.functionName = triggerName;
    }

    public String getContractId() {
        return contractId;
    }

    public String getContractInstanceId() {
        return contractInstanceId;
    }

    public String getFunctionName() {
        return functionName;
    }
}

package models.dto.requests.event;

public class EventTriggerRequest {
    private final String contractId;
    private final String contractInstanceId;
    private final String obligationName;

    public EventTriggerRequest(String contractId, String contractInstanceId, String obligationName) {
        this.contractId = contractId;
        this.contractInstanceId = contractInstanceId;
        this.obligationName = obligationName;
    }

    public String getContractId() {
        return contractId;
    }

    public String getContractInstanceId() {
        return contractInstanceId;
    }

    public String getObligationName() {
        return obligationName;
    }

    @Override
    public String toString() {
        return "EventTriggerRequest{" +
                "contractId='" + contractId + '\'' +
                ", contractInstanceId='" + contractInstanceId + '\'' +
                ", functionName='" + obligationName + '\'' +
                '}';
    }
}

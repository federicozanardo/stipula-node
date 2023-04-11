package models.dto.requests.event;

public class CreateEventRequest {
    /**
     * Name of the obligation function to execute at a specific time.
     */
    private final String obligationFunctionName;

    /**
     * When execute the obligation function.
     */
    private final int time;

    public CreateEventRequest(String obligationFunctionName, int time) {
        this.obligationFunctionName = obligationFunctionName;
        this.time = time;
    }

    public String getObligationFunctionName() {
        return obligationFunctionName;
    }

    public int getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "CreateEventRequest{" +
                "obligationFunctionName='" + obligationFunctionName + '\'' +
                ", time=" + time +
                '}';
    }
}

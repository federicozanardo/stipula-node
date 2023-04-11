package models.dto.responses;

public class Response {
    private final int statusCode;
    private final String statusMessage;
    private final String type;

    public Response(int statusCode, String statusMessage, String type) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.type = type;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Response{" +
                "statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}

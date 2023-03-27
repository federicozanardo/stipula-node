package models.dto.responses;

public class Response {
<<<<<<< Updated upstream
    private final boolean success;

    public Response(boolean success) {
        this.success = success;
=======
    private final int statusCode;
    private final String statusMessage;
    private final String type;

    public Response(int statusCode, String statusMessage, String type) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.type = type;
>>>>>>> Stashed changes
    }

    public boolean isSuccess() {
        return success;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Response{" +
<<<<<<< Updated upstream
                "success=" + success +
=======
                "statusCode=" + statusCode +
                ", statusMessage='" + statusMessage + '\'' +
                ", type='" + type + '\'' +
>>>>>>> Stashed changes
                '}';
    }
}

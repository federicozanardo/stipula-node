package models.dto.responses;

public class ResponseData extends Response {
    private final Object data;

<<<<<<< Updated upstream
    public ResponseData(boolean status, Object data) {
        super(status);
=======
    public ResponseData(int statusCode, String statusMessage, Object data, String type) {
        super(statusCode, statusMessage, type);
>>>>>>> Stashed changes
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ResponseData{" +
                "data=" + data +
                '}';
    }
}

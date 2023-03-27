package models.dto.responses;

public class ResponseData extends Response {
    private final Object data;

    public ResponseData(int statusCode, String statusMessage, Object data, String type) {
        super(statusCode, statusMessage, type);
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

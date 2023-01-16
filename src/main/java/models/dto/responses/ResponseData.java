package models.dto.responses;

public class ResponseData extends Response {
    private final Object data;

    public ResponseData(boolean status, Object data) {
        super(status);
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

package models.dto.responses;

public class Response {
    private final boolean success;

    public Response(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "Response{" +
                "success=" + success +
                '}';
    }
}

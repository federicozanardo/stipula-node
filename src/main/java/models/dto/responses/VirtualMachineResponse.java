package models.dto.responses;

public class VirtualMachineResponse {
    private final int statusCode;
    private Object data = null;

    public VirtualMachineResponse(int statusCode) {
        this.statusCode = statusCode;
    }

    public VirtualMachineResponse(int statusCode, Object data) {
        this.statusCode = statusCode;
        this.data = data;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "VirtualMachineResponse{" +
                "statusCode=" + statusCode +
                ", data=" + data +
                '}';
    }
}

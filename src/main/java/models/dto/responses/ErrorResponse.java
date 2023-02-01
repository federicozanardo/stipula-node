package models.dto.responses;

public class ErrorResponse extends ResponseData {
    private final int errorCode;
    private final String errorMessage;

    public ErrorResponse(int errorCode, String errorMessage, Object data) {
        super(false, data);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public ErrorResponse(int errorCode, String errorMessage) {
        super(false, null);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "errorCode=" + errorCode +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}

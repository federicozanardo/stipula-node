package models.dto.responses.error;

import models.dto.responses.ResponseCodes;
import models.dto.responses.ResponseData;

public class ErrorDataResponse extends ResponseData {
    private final String errorMessage;

    public ErrorDataResponse(int statusCode, String errorMessage) {
        super(statusCode, ResponseCodes.getMessage(statusCode), null, ErrorDataResponse.class.getSimpleName());
        this.errorMessage = errorMessage;
    }

    public ErrorDataResponse(int statusCode, String errorMessage, Object data) {
        super(statusCode, ResponseCodes.getMessage(statusCode), data, ErrorDataResponse.class.getSimpleName());
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "ErrorDataResponse{" +
                "errorMessage='" + errorMessage + '\'' +
                '}';
    }
}

package models.dto.responses.success;

import models.dto.responses.ResponseCodes;
import models.dto.responses.ResponseData;

public class SuccessDataResponse extends ResponseData {
    public SuccessDataResponse(Object data) {
        super(200, ResponseCodes.getMessage(200), data, SuccessDataResponse.class.getSimpleName());
    }

    public SuccessDataResponse(int statusCode, Object data) {
        super(statusCode, ResponseCodes.getMessage(statusCode), data, SuccessDataResponse.class.getSimpleName());
    }
}

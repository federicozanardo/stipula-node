package models.dto.responses;

public class SuccessDataResponse extends ResponseData {
    public SuccessDataResponse(Object data) {
        super(true, data);
    }
}

package models.dto.requests.property;

import models.dto.requests.Message;

public class GetPropertiesByAddress extends Message {
    private final String address;

    public GetPropertiesByAddress(String address) {
        super(GetPropertiesByAddress.class.getSimpleName());
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "GetAssetsByAddress{" +
                "address='" + address + '\'' +
                '}';
    }
}

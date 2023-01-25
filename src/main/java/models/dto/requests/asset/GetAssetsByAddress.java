package models.dto.requests.asset;

import models.dto.requests.Message;

public class GetAssetsByAddress extends Message {
    private final String address;

    public GetAssetsByAddress(String address) {
        super(GetAssetsByAddress.class.getSimpleName());
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

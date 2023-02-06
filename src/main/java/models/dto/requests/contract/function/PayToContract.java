package models.dto.requests.contract.function;

public class PayToContract {
    private final String propertyId;
    private final String address;
    private final String unlockScript;

    public PayToContract(String propertyId, String address, String unlockScript) {
        this.propertyId = propertyId;
        this.address = address;
        this.unlockScript = unlockScript;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public String getAddress() {
        return address;
    }

    public String getUnlockScript() {
        return unlockScript;
    }

    @Override
    public String toString() {
        return "PayToContract{" +
                "propertyId='" + propertyId + '\'' +
                ", address='" + address + '\'' +
                ", unlockScript='" + unlockScript + '\'' +
                '}';
    }
}

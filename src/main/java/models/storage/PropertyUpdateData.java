package models.storage;

public class PropertyUpdateData {
    private final String propertyId;
    private final String contractInstanceId;
    private final String unlockScript;

    public PropertyUpdateData(String propertyId, String contractInstanceId, String unlockScript) {
        this.propertyId = propertyId;
        this.contractInstanceId = contractInstanceId;
        this.unlockScript = unlockScript;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public String getContractInstanceId() {
        return contractInstanceId;
    }

    public String getUnlockScript() {
        return unlockScript;
    }
}

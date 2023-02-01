package models.dto.requests.contract.function;

import models.contract.Property;

public class PayToContract {
    private final Property property;
    private final String unlockScript;

    public PayToContract(Property property, String unlockScript) {
        this.property = property;
        this.unlockScript = unlockScript;
    }

    public Property getProperty() {
        return property;
    }

    public String getUnlockScript() {
        return unlockScript;
    }

    @Override
    public String toString() {
        return "PayToContract{" +
                "property=" + property +
                ", unlockScript='" + unlockScript + '\'' +
                '}';
    }
}
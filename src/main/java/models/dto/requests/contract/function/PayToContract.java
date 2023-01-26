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

    /*public boolean verify() {
        BigDecimal zeroValue = new BigDecimal(0);
        if (this.singleUseSeal.getAmount().getValue().compareTo(zeroValue) <= 0) {
            return false;
        }

        BigDecimal supply = new BigDecimal(this.singleUseSeal.getAssetConfig().getSupply());
        if (this.singleUseSeal.getAmount().getValue().compareTo(supply) > 0) {
            return false;
        }


        return true;
    }*/
}
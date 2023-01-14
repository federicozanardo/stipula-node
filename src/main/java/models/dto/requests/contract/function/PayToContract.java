package models.dto.requests.contract.function;

import models.contract.SingleUseSeal;

public class PayToContract {
    private final SingleUseSeal singleUseSeal;
    private final String unlockScript;

    public PayToContract(SingleUseSeal singleUseSeal, String unlockScript) {
        this.singleUseSeal = singleUseSeal;
        this.unlockScript = unlockScript;
    }

    public SingleUseSeal getSingleUseSeal() {
        return singleUseSeal;
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
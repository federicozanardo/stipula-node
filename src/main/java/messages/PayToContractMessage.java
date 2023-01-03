package messages;

import asset.AssetConfig;
import vm.contract.SingleUseSeal;
import vm.contract.UnlockingScript;

import java.math.BigDecimal;

public class PayToContractMessage<T extends AssetConfig> extends Message {
    private final String contractInstanceId;
    private final SingleUseSeal<T> singleUseSeal;
    private final UnlockingScript unlockingScript;

    public PayToContractMessage(String contractInstanceId, SingleUseSeal<T> singleUseSeal, UnlockingScript unlockingScript) {
        this.contractInstanceId = contractInstanceId;
        this.singleUseSeal = singleUseSeal;
        this.unlockingScript = unlockingScript;
    }

    public String getContractInstanceId() {
        return contractInstanceId;
    }

    public SingleUseSeal<T> getSingleUseSeal() {
        return singleUseSeal;
    }

    public UnlockingScript getUnlockingScript() {
        return unlockingScript;
    }

    public boolean verify() {
        BigDecimal zeroValue = new BigDecimal(0);
        if (this.singleUseSeal.getAmount().getValue().compareTo(zeroValue) <= 0) {
            return false;
        }

        BigDecimal supply = new BigDecimal(this.singleUseSeal.getAssetConfig().getSupply());
        if (this.singleUseSeal.getAmount().getValue().compareTo(supply) > 0) {
            return false;
        }


        return true;
    }
}
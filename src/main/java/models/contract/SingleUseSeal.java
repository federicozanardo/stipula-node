package models.contract;

import vm.types.FloatType;

import java.io.Serializable;

public class SingleUseSeal implements Serializable {
    private final String assetId;
    private final FloatType amount;
    private final String lockScript;

    public SingleUseSeal(String assetId, FloatType amount, String publicKeyHash) {
        this.assetId = assetId;
        this.amount = amount;
        this.lockScript = "DUP\nSHA256\nPUSH str " + publicKeyHash + "\nEQUAL\nCHECKSIG\nHALT\n";
    }

    public String getAssetId() {
        return assetId;
    }

    public FloatType getAmount() {
        return amount;
    }

    public String getLockScript() {
        return lockScript;
    }

    @Override
    public String toString() {
        return "SingleUseSeal{" +
                "assetId='" + assetId + '\'' +
                ", amount=" + amount +
                ", lockScript='" + lockScript + '\'' +
                '}';
    }
}
package vm.contract;

import asset.AssetConfig;
import vm.types.FloatType;

public class SingleUseSeal {
    private final String id;
    private final String assetId;
    private final FloatType amount;
    private final String lockScript;

    public SingleUseSeal(String id, String assetId, FloatType amount, String publicKeyHash) {
        this.id = id;
        this.assetId = assetId;
        this.amount = amount;
        this.lockScript = "DUP\nSHA256\nPUSH str " + publicKeyHash + "\nEQUAL\nCHECKSIG\n";
    }

    public String getId() {
        return id;
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
}
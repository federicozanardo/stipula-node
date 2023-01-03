package vm.contract;

import asset.AssetConfig;
import vm.types.FloatType;

public class SingleUseSeal<T extends AssetConfig> {
    private final String id;
    private final T assetConfig;
    private final FloatType amount;
    private final String lockingScript;

    public SingleUseSeal(String id, T assetConfig, FloatType amount, String publicKeyHash) {
        this.id = id;
        this.assetConfig = assetConfig;
        this.amount = amount;
        this.lockingScript = "DUP\n SHA256\n PUSH str " + publicKeyHash + "\n EQUAL\n CHECKSIG\n";
    }

    public String getId() {
        return id;
    }

    public T getAssetConfig() {
        return assetConfig;
    }

    public FloatType getAmount() {
        return amount;
    }

    public String getLockingScript() {
        return lockingScript;
    }
}
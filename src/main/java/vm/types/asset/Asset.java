package vm.types.asset;

import vm.types.FloatType;

public class Asset {
    private final String assetId;
    private final FloatType amount;

    public Asset(String assetId, FloatType amount) {
        this.assetId = assetId;
        this.amount = amount;
    }

    public String getAssetConfig() {
        return assetId;
    }

    public FloatType getAmount() {
        return amount;
    }
}

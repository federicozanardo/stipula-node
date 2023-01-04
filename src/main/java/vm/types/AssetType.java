package vm.types;

public class AssetType extends Type {
    private final String assetId;
    private final FloatType amount;

    public AssetType(String assetId, FloatType amount) {
        this.assetId = assetId;
        this.amount = amount;
    }

    @Override
    public String getType() {
        return "asset";
    }

    @Override
    public FloatType getValue() {
        return this.amount;
    }

    public String getAssetId() {
        return this.assetId;
    }
}
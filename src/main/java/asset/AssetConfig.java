package asset;

public abstract class AssetConfig {
    private final String assetId;
    private final String assetName;
    private final String unitName;
    private final int supply;
    private final int decimals;

    public AssetConfig(String assetId, String assetName, String unitName, int supply, int decimals) {
        this.assetId = assetId;
        this.assetName = assetName;
        this.unitName = unitName;
        this.supply = supply;
        this.decimals = decimals;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public String getUnitName() {
        return unitName;
    }

    public int getSupply() {
        return supply;
    }

    public int getDecimals() {
        return decimals;
    }

    @Override
    public String toString() {
        return "AssetConfig{" +
                "assetId='" + assetId + '\'' +
                ", assetName='" + assetName + '\'' +
                ", unitName='" + unitName + '\'' +
                ", supply=" + supply +
                ", decimals=" + decimals +
                '}';
    }
}

package models.assets;

public class Asset {
    private final String id;
    private final AssetConfig asset;

    public Asset(String id, AssetConfig asset) {
        this.id = id;
        this.asset = asset;
    }

    public String getId() {
        return id;
    }

    public AssetConfig getAsset() {
        return asset;
    }
}

package models.assets;

import java.io.Serializable;

public class Asset implements Serializable {
    /**
     * It consists of an alphanumeric string to uniquely refer to an asset.
     */
    private final String id;

    /**
     * Asset configuration.
     */
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

    @Override
    public String toString() {
        return "Asset{" +
                "id='" + id + '\'' +
                ", asset=" + asset +
                '}';
    }
}

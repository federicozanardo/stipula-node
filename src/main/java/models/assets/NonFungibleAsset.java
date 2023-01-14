package models.assets;

public class NonFungibleAsset extends AssetConfig {
    public NonFungibleAsset(String assetId, String assetName, String unitName) {
        super(assetId, assetName, unitName, 1, 0);
    }
}
package models.assets;

public class FungibleAsset extends AssetConfig {
    public FungibleAsset(String assetId, String assetName, String unitName, int supply, int decimals) {
        super(assetId, assetName, unitName, supply, decimals);
    }
}
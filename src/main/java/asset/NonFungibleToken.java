package asset;

public class NonFungibleToken extends AssetConfig {
    public NonFungibleToken(String assetId, String assetName, String unitName) {
        super(assetId, assetName, unitName, 1, 0);
    }
}
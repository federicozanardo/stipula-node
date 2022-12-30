package vm.types.asset;

public class FungibleToken extends AssetConfig {
    public FungibleToken(String assetId, String assetName, String unitName, int supply, int decimals) {
        super(assetId, assetName, unitName, supply, decimals);
    }
}

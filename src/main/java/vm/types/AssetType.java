package vm.types;

import java.math.BigDecimal;

public class AssetType extends Type {
    private final FloatType amount;

    public AssetType(FloatType amount) {
        this.amount = amount;
    }

    public FloatType getAmount() {
        return amount;
    }

    @Override
    public String getType() {
        return "asset";
    }

    @Override
    public BigDecimal getValue() {
        return this.amount.getValue();
    }
}
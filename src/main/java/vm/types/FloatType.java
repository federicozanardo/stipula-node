package vm.types;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FloatType extends Type {
    private final Integer value;
    private final int decimals;

    public FloatType() {
        this.value = 0;
        this.decimals = 2;
    }

    public FloatType(Integer value) {
        this.value = value;
        this.decimals = 2;
    }

    public FloatType(Integer value, int decimals) {
        this.value = value;
        this.decimals = decimals;
    }

    @Override
    public String getType() {
        return "float";
    }

    @Override
    public BigDecimal getValue() {
        BigDecimal d = new BigDecimal(this.value / Math.pow(10, this.decimals));
        return d.setScale(this.decimals, RoundingMode.CEILING);
    }

    public Integer getInteger() {
        return value;
    }

    public Integer getDecimals() {
        return decimals;
    }
}

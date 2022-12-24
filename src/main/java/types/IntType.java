package types;

public class IntType extends Type {
    final private String type = "int";
    private final Integer value;

    public IntType() {
        this.value = 0;
    }

    public IntType(Integer value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}

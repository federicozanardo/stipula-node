package types;

public class TraceChange {
    private final Type value;
    private final boolean isChanged;

    public TraceChange(Type value) {
        this.value = value;
        this.isChanged = false;
    }

    public TraceChange(Type value, boolean isChanged) {
        this.value = value;
        this.isChanged = isChanged;
    }

    public Type getValue() {
        return value;
    }

    public boolean isChanged() {
        return isChanged;
    }
}

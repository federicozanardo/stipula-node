package vm.types.address;

import vm.types.Type;

public class AddrType extends Type {
    final private String type = "addr";
    private final Address value;

    public AddrType() {
        this.value = null;
    }

    public AddrType(Address value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getValue() {
        return value.getAddress() + " " + value.getPublicKey();
    }

    public String getAddress() {
        return value.getAddress();
    }

    public String getPublicKey() {
        return value.getPublicKey();
    }
}

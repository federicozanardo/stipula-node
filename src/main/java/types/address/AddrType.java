package types.address;

import types.Type;

public class AddrType extends Type {
    final private String type = "addr";
    private Address value;

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
        return value.getPublicKey();
    }

}

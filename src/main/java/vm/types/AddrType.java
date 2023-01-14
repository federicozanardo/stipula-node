package vm.types;

import models.address.Address;
import vm.types.Type;

public class AddrType extends Type {
    private final Address value;

    public AddrType() {
        this.value = null;
    }

    public AddrType(Address value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return "addr";
    }

    @Override
    public String getValue() {
        assert value != null;
        return value.getAddress() + " " + value.getPublicKey();
    }

    public String getAddress() {
        assert value != null;
        return value.getAddress();
    }

    public String getPublicKey() {
        assert value != null;
        return value.getPublicKey();
    }

    @Override
    public String toString() {
        return "AddrType{" +
                "value=" + value +
                '}';
    }
}

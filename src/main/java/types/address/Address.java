package types.address;

public class Address {
    private final String publicKey;

    public Address(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }
}

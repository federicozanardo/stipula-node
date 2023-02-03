package exceptions.storage;

public class PropertiesNotFoundException extends Exception {
    public PropertiesNotFoundException() {
        super();
    }

    public PropertiesNotFoundException(String address) {
        super("There are no funds associated to the address = " + address);
    }
}

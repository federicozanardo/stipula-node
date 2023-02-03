package exceptions.storage;

public class PropertyNotFoundException extends Exception {
    public PropertyNotFoundException() {
        super();
    }

    public PropertyNotFoundException(String address, String propertyId) {
        super("The property with id = " + propertyId +
                ", associated to the address = " + address +
                ", does not exist in the storage");
    }
}

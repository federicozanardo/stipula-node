package exceptions.storage;

public class PropertyNotFoundException extends Exception {
    public PropertyNotFoundException() {
        super();
    }

    public PropertyNotFoundException(String propertyId) {
        super("The property with id = " + propertyId + " does not exist in the storage");
    }
}

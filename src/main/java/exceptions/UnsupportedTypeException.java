package exceptions;

public class UnsupportedTypeException extends Exception {
    public UnsupportedTypeException() {
        super();
    }

    public UnsupportedTypeException(String message) {
        super(message);
    }
}

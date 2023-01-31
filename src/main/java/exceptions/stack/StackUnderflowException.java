package exceptions.stack;


public class StackUnderflowException extends Exception {

    public StackUnderflowException() {
        super();
    }

    public StackUnderflowException(String message) {
        super(message);
    }
}

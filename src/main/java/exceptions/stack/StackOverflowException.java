package exceptions.stack;


import exceptions.trap.TrapException;
import vm.trap.TrapErrorCodes;

public class StackOverflowException extends TrapException {

    public StackOverflowException() {
        super(TrapErrorCodes.STACK_OVERFLOW);
    }

}

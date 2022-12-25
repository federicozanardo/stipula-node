package exceptions.stack;

import exceptions.trap.TrapException;
import vm.trap.TrapErrorCodes;

public class StackUnderflowException extends TrapException {

    public StackUnderflowException() {
        super(TrapErrorCodes.STACK_UNDERFLOW);
    }

}

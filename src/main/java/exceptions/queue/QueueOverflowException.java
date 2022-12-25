package exceptions.queue;

import vm.trap.TrapErrorCodes;

public class QueueOverflowException extends QueueException {

    public QueueOverflowException() {
        super(TrapErrorCodes.QUEUE_OVERFLOW);
    }

}

package exceptions.queue;


import vm.trap.TrapErrorCodes;

public class QueueUnderflowException extends QueueException {

    public QueueUnderflowException() {
        super(TrapErrorCodes.QUEUE_UNDERFLOW);
    }

}

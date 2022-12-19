package exceptions.queue;


import trap.TrapErrorCodes;

public class QueueUnderflowException extends QueueException {

  public QueueUnderflowException() {
    super(TrapErrorCodes.QUEUE_UNDERFLOW);
  }
  
}

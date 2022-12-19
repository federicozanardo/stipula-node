package exceptions.queue;

import trap.TrapErrorCodes;

public class QueueException extends Exception {
  private final TrapErrorCodes code;

  public QueueException(TrapErrorCodes code) {
		super();
		this.code = code;
	}

  public TrapErrorCodes getCode() {
    return this.code;
  }
}

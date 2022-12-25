package exceptions.trap;

import vm.trap.TrapErrorCodes;

public class TrapException extends Exception {
    private final TrapErrorCodes code;

    public TrapException(TrapErrorCodes code) {
        super();
        this.code = code;
    }

    public TrapErrorCodes getCode() {
        return this.code;
    }
}

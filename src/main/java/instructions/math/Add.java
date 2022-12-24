package instructions.math;

import exceptions.trap.TrapException;
import instructions.MathInstruction;
import trap.TrapErrorCodes;
import types.IntType;
import types.Type;

public class Add extends MathInstruction {
    final private Type first;
    final private Type second;

    public Add(Type first, Type second) {
        super("ADD");
        this.first = first;
        this.second = second;
    }

    @Override
    public IntType execute() throws TrapException {
        if (this.checkTypes()) {
            IntType firstVal = new IntType((Integer) first.getValue());
            IntType secondVal = new IntType((Integer) second.getValue());
            return new IntType(firstVal.getValue() + secondVal.getValue());
        } else {
            throw new TrapException(TrapErrorCodes.INCORRECT_TYPE);
        }
    }

    @Override
    public boolean checkTypes() {
        return first.getType().equals("int") && second.getType().equals("int");
    }
}

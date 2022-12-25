package vm.instructions.math;

import exceptions.trap.TrapException;
import vm.instructions.MathInstruction;
import vm.trap.TrapErrorCodes;
import vm.types.IntType;
import vm.types.Type;

public class Mul extends MathInstruction {
    final private Type first;
    final private Type second;

    public Mul(Type first, Type second) {
        super("MUL");
        this.first = first;
        this.second = second;
    }

    @Override
    public IntType execute() throws TrapException {
        if (this.checkTypes()) {
            IntType firstVal = new IntType((Integer) first.getValue());
            IntType secondVal = new IntType((Integer) second.getValue());
            return new IntType(firstVal.getValue() * secondVal.getValue());
        } else {
            throw new TrapException(TrapErrorCodes.INCORRECT_TYPE);
        }
    }

    @Override
    public boolean checkTypes() {
        return first.getType().equals("int") && second.getType().equals("int");
    }
}

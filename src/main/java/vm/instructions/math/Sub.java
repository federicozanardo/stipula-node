package vm.instructions.math;

import exceptions.trap.TrapException;
import vm.instructions.MathInstruction;
import vm.trap.TrapErrorCodes;
import vm.types.IntType;
import vm.types.Type;

public class Sub extends MathInstruction {
    final private Type first;
    final private Type second;

    public Sub(Type first, Type second) {
        super("SUB");
        this.first = first;
        this.second = second;
    }

    @Override
    public IntType execute() throws TrapException {
        if (this.checkTypes()) {
            IntType firstVal = new IntType((Integer) first.getValue());
            IntType secondVal = new IntType((Integer) second.getValue());
            return new IntType(firstVal.getValue() - secondVal.getValue());
        } else {
            throw new TrapException(TrapErrorCodes.INCORRECT_TYPE);
        }
    }

    @Override
    public boolean checkTypes() {
        return first.getType().equals("int") && second.getType().equals("int");
    }
}

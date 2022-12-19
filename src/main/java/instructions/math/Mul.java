package instructions.math;

import exceptions.trap.TrapException;
import instructions.MathInstruction;
import trap.TrapErrorCodes;
import types.IntType;
import types.Type;

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
      return new IntType((Integer) firstVal.getValue() * (Integer) secondVal.getValue());
    } else {
      throw new TrapException(TrapErrorCodes.INCORRECT_TYPE);
    }
  }

  @Override
  public boolean checkTypes() {
    if (!first.getType().equals("int") || !second.getType().equals("int")) {
      return false;
    }
    return true;
  }
}

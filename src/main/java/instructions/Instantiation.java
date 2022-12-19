package instructions;

import exceptions.trap.TrapException;
import trap.TrapErrorCodes;
import types.IntType;
import types.StringType;
import types.Type;

public class Instantiation extends Instruction {
  final private Type variable;

  public Instantiation(Type variable) {
    super("INST");
    this.variable = variable;
  }

  @Override
  public Type execute() throws TrapException {
    switch (variable.getType()) {
      case "int":
        // dataSpace.put(variableName, new IntType());
        return new IntType();
      case "str":
        // dataSpace.put(variableName, new StringType());
        return new StringType();
      default:
        throw new TrapException(TrapErrorCodes.TYPE_DOES_NOT_EXIST);
    }
  }
}

package instructions;

public abstract class MathInstruction extends Instruction {
  public MathInstruction(String name) {
    super(name);
  }

  public abstract boolean checkTypes();
}

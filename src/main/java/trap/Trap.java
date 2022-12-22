package trap;

import java.util.HashMap;

import datastructures.Stack;
import exceptions.stack.StackOverflowException;
import types.StrType;

public class Trap {
  private Stack<StrType> stack = new Stack<StrType>(100);
  private HashMap<TrapErrorCodes, String> map = new HashMap<TrapErrorCodes, String>() {
    {
      this.put(TrapErrorCodes.ERROR_CODE_DOES_NOT_EXISTS, "This error code does not exist");
      this.put(TrapErrorCodes.INCORRECT_TYPE, "Wrong type for this instruction");
      this.put(TrapErrorCodes.INSTRUCTION_DOES_NOT_EXISTS, "This instruction does not exist");
      this.put(TrapErrorCodes.LABEL_DOES_NOT_EXISTS, "This label does not exist in the code");
      this.put(TrapErrorCodes.MISS_HALT_INSTRUCTION, "Last instruction must be HALT");
      this.put(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, "There are not enough arguments for the current instruction");
      this.put(TrapErrorCodes.QUEUE_OVERFLOW, "Queue overflow");
      this.put(TrapErrorCodes.QUEUE_UNDERFLOW, "Queue underflow");
      this.put(TrapErrorCodes.STACK_OVERFLOW, "Stack overflow");
      this.put(TrapErrorCodes.STACK_UNDERFLOW, "Stack underflow");
      this.put(TrapErrorCodes.TOO_MANY_ARGUMENTS, "There are too many arguments for the current instruction");
      this.put(TrapErrorCodes.TYPE_DOES_NOT_EXIST, "This type does not exist");
      this.put(TrapErrorCodes.VARIABLE_ALREADY_EXIST, "This variable already exist in the stack");
      this.put(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, "This variable does not exist in the stack");
    }
  };

  public void raiseError(TrapErrorCodes errorCode, int line) {
    if (!map.containsKey(errorCode)) {
      this.raiseError(TrapErrorCodes.ERROR_CODE_DOES_NOT_EXISTS, line);
    }
    this.pushTrapError(errorCode, map.get(errorCode), line);
  }

  public void raiseError(TrapErrorCodes errorCode, int line, String instruction) {
    if (!map.containsKey(errorCode)) {
      this.raiseError(TrapErrorCodes.ERROR_CODE_DOES_NOT_EXISTS, line, instruction);
    }
    this.pushTrapError(errorCode, map.get(errorCode), line, instruction);
  }

  private void pushTrapError(TrapErrorCodes errorCode, String errorMessage, int line) {
    try {
      this.stack
          .push(new StrType(errorCode.toString() + " at line " + Integer.toString(line) + ": " + errorMessage));
    } catch (StackOverflowException error) {
      System.exit(-1);
    }
  }

  private void pushTrapError(TrapErrorCodes errorCode, String errorMessage, int line, String instruction) {
    try {
      this.stack.push(new StrType(errorCode.toString() + " at line " + Integer.toString(line) + ": " + errorMessage
          + "\nInstruction: " + instruction));
    } catch (StackOverflowException error) {
      System.exit(-1);
    }
  }

  public boolean isStackEmpty() {
    return this.stack.isEmpty();
  }

  public String printStack() {
    return this.stack.toString();
  }
}

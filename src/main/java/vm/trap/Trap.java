package vm.trap;

import exceptions.stack.StackOverflowException;
import lib.datastructures.Stack;
import vm.types.StrType;

import java.util.HashMap;

public class Trap {
    private final int offset;
    private final Stack<StrType> stack = new Stack<StrType>(100);
    private final HashMap<TrapErrorCodes, String> errors = new HashMap<TrapErrorCodes, String>() {
        {
            this.put(TrapErrorCodes.ASSET_IDS_DOES_NOT_MATCH, "Asset ids do not match");
            this.put(TrapErrorCodes.CRYPTOGRAPHIC_ALGORITHM_DOES_NOT_EXISTS, "The cryptographic algorithm requested does not exist");
            this.put(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, "The decimals of the two variables do not match");
            this.put(TrapErrorCodes.DIVISION_BY_ZERO, "Division by zero");
            this.put(TrapErrorCodes.ERROR_CODE_DOES_NOT_EXISTS, "This error code does not exist");
            this.put(TrapErrorCodes.ELEMENTS_ARE_NOT_EQUAL, "The two elements are not equal");
            this.put(TrapErrorCodes.INCORRECT_TYPE, "Wrong type for this instruction");
            this.put(TrapErrorCodes.INCORRECT_TYPE_OR_TYPE_DOES_NOT_EXIST, "Wrong type for this instruction or this type does not exist");
            this.put(TrapErrorCodes.INSTRUCTION_DOES_NOT_EXISTS, "This instruction does not exist");
            this.put(TrapErrorCodes.LABEL_DOES_NOT_EXISTS, "This label does not exist in the code");
            this.put(TrapErrorCodes.LESS_THAN_ZERO, "The current value is less than zero");
            this.put(TrapErrorCodes.MISS_HALT_INSTRUCTION, "Last instruction must be HALT");
            this.put(TrapErrorCodes.NEGATIVE_VALUE, "The current value is negative");
            this.put(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, "There are not enough arguments for the current instruction");
            this.put(TrapErrorCodes.QUEUE_OVERFLOW, "Queue overflow");
            this.put(TrapErrorCodes.QUEUE_UNDERFLOW, "Queue underflow");
            this.put(TrapErrorCodes.STACK_OVERFLOW, "Stack overflow");
            this.put(TrapErrorCodes.STACK_UNDERFLOW, "Stack underflow");
            this.put(TrapErrorCodes.TOO_MANY_ARGUMENTS, "There are too many arguments for the current instruction");
            this.put(TrapErrorCodes.TYPE_DOES_NOT_EXIST, "This type does not exist");
            this.put(TrapErrorCodes.VARIABLE_ALREADY_EXIST, "This variable already exist in the stack");
            this.put(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, "This variable does not exist in the stack");
            this.put(TrapErrorCodes.ZERO_VALUE, "The current value is zero");
        }
    };

    public Trap(int offset) {
        this.offset = offset;
    }

    /**
     * @param errorCode
     * @param line
     */
    public void raiseError(TrapErrorCodes errorCode, int line) {
        if (!errors.containsKey(errorCode)) {
            this.raiseError(TrapErrorCodes.ERROR_CODE_DOES_NOT_EXISTS, line);
        }
        this.pushTrapError(errorCode, errors.get(errorCode), line);
    }

    /**
     * @param errorCode
     * @param line
     * @param instruction
     */
    public void raiseError(TrapErrorCodes errorCode, int line, String instruction) {
        if (!errors.containsKey(errorCode)) {
            this.raiseError(TrapErrorCodes.ERROR_CODE_DOES_NOT_EXISTS, line, instruction);
        }
        this.pushTrapError(errorCode, errors.get(errorCode), line, instruction);
    }

    /**
     * @param errorCode
     * @param errorMessage
     * @param line
     */
    private void pushTrapError(TrapErrorCodes errorCode, String errorMessage, int line) {
        try {
            stack.push(new StrType(errorCode.toString() +
                    " at line " + (line + 1 + offset) +
                    ": " + errorMessage));
        } catch (StackOverflowException error) {
            System.out.println("pushTrapError: Error while pushing a trap error in the stack" +
                    "\nerrorCode: " + errorCode +
                    "\nerrorMessage: " + errorMessage +
                    "\nline: " + line);
        }
    }

    /**
     * @param errorCode
     * @param errorMessage
     * @param line
     */
    public void pushTrapError(String errorCode, String errorMessage, int line) {
        try {
            stack.push(new StrType(errorCode +
                    " at line " + (line + 1 + offset) +
                    ": " + errorMessage));
        } catch (StackOverflowException error) {
            System.out.println("pushTrapError: Error while pushing a trap error in the stack" +
                    "\nerrorCode: " + errorCode +
                    "\nerrorMessage: " + errorMessage +
                    "\nline: " + line);
        }
    }

    /**
     * @param errorCode
     * @param errorMessage
     * @param line
     * @param instruction
     */
    private void pushTrapError(TrapErrorCodes errorCode, String errorMessage, int line, String instruction) {
        try {
            stack.push(new StrType(errorCode.toString() +
                    " at line " + (line + 1 + offset) +
                    ": " + errorMessage
                    + "\nInstruction: " + instruction));
        } catch (StackOverflowException error) {
            System.out.println("pushTrapError: Error while pushing a trap error in the stack" +
                    "\nerrorCode: " + errorCode +
                    "\nerrorMessage: " + errorMessage +
                    "\nline: " + line +
                    "\ninstruction: " + instruction);
        }
    }

    /**
     * @param errorCode
     * @param errorMessage
     * @param line
     * @param instruction
     */
    public void pushTrapError(String errorCode, String errorMessage, int line, String instruction) {
        try {
            stack.push(new StrType(errorCode +
                    " at line " + (line + 1 + offset) +
                    ": " + errorMessage
                    + "\nInstruction: " + instruction));
        } catch (StackOverflowException error) {
            System.out.println("pushTrapError: Error while pushing a trap error in the stack" +
                    "\nerrorCode: " + errorCode +
                    "\nerrorMessage: " + errorMessage +
                    "\nline: " + line +
                    "\ninstruction: " + instruction);
        }
    }

    /**
     * @return
     */
    public boolean isStackEmpty() {
        return stack.isEmpty();
    }

    /**
     * @return
     */
    public String printStack() {
        return stack.toString();
    }
}

package vm;

import exceptions.stack.StackOverflowException;
import exceptions.stack.StackUnderflowException;
import lib.datastructures.Stack;
import vm.trap.Trap;
import vm.trap.TrapErrorCodes;
import vm.types.BoolType;
import vm.types.StrType;
import vm.types.Type;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import static lib.crypto.Crypto.getPublicKeyFromString;
import static lib.crypto.Crypto.verify;

public class ScriptVirtualMachine {
    private final String[] instructions;
    private final String propertyId;
    private boolean isRunning = true;
    private int executionPointer = -1;
    private final Stack<Type> stack = new Stack<Type>(10);
    private final Trap trap;

    public ScriptVirtualMachine(String[] instructions, String propertyId) {
        this.instructions = instructions;
        this.propertyId = propertyId;
        this.trap = new Trap(0);
    }

    public boolean execute() throws Exception {
        while (isRunning) {
            if (!trap.isStackEmpty()) {
                haltProgramExecution();
                break;
            }

            try {
                executionPointer++;
                String singleInstruction = this.instructions[executionPointer].trim();
                String[] instruction = singleInstruction.split(" ");

                if (!(instruction.length == 1 && instruction[0].endsWith(":"))) {
                    switch (instruction[0]) {
                        case "PUSH":
                            this.pushOperation(instruction);
                            break;
                        case "DUP":
                            this.dupOperation(instruction);
                            break;
                        case "SHA256":
                            this.sha256Operation(instruction);
                            break;
                        case "EQUAL":
                            this.equalOperation(instruction);
                            break;
                        case "CHECKSIG":
                            this.checksigOperation(instruction);
                            break;
                        case "HALT":
                            this.haltOperation(instruction);
                            break;
                        default:
                            trap.raiseError(TrapErrorCodes.INSTRUCTION_DOES_NOT_EXISTS, executionPointer, Arrays.toString(instruction));
                    }
                }
            } catch (StackOverflowException error) {
                trap.raiseError(TrapErrorCodes.STACK_OVERFLOW, executionPointer);
            } catch (StackUnderflowException error) {
                trap.raiseError(TrapErrorCodes.STACK_UNDERFLOW, executionPointer);
            }
        }

        if (!trap.isStackEmpty()) {
            System.out.println("\nexecute: Errors in the stack");
            System.out.println(trap.printStack());
            return false;
        }

        if (stack.isEmpty()) {
            System.out.println("execute: The stack is empty");
            return false;
        }

        Type value = stack.pop();

        if (!stack.isEmpty()) {
            System.out.println("execute: There is more than one value in the stack");
            return false;
        }

        if (!value.getType().equals("bool")) {
            System.out.println("execute: The last value in the stack is not a boolean");
            return false;
        }

        BoolType boolVal = (BoolType) value;

        if (!boolVal.getValue()) {
            System.out.println("execute: The funds can't be used. The script can't be unlocked");
            return false;
        }

        System.out.println("execute: Final state of the execution below");

        System.out.println("\nGlobal state of the execution" +
                "\nrunning -> " + isRunning +
                "\nexecutionPointer -> " + executionPointer +
                "\nlength of the program -> " + instructions.length);

        return !isRunning;
    }

    private void haltProgramExecution() {
        isRunning = false;
    }

    private boolean argumentsAreMoreThan(String[] instruction, int num) {
        if ((instruction.length - 1) > num) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return true;
        }
        return false;
    }

    private boolean argumentsAreLessThan(String[] instruction, int num) {
        if ((instruction.length - 1) < num) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return true;
        }
        return false;
    }

    private void haltOperation(String[] instruction) {
        if (this.argumentsAreMoreThan(instruction, 0)) {
            return;
        }

        // Terminate the execution
        this.haltProgramExecution();
    }

    private void pushOperation(String[] instruction) throws StackOverflowException, NoSuchAlgorithmException {
        if (this.argumentsAreLessThan(instruction, 2)) {
            return;
        }

        if (this.argumentsAreMoreThan(instruction, 2)) {
            return;
        }

        String type = instruction[1];
        String value = instruction[2];

        if (!type.equals("str")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        stack.push(new StrType(value));
    }

    private void dupOperation(String[] instruction) throws StackUnderflowException, StackOverflowException {
        if (this.argumentsAreMoreThan(instruction, 0)) {
            return;
        }

        Type value = stack.pop();
        stack.push(value);
        stack.push(value);
    }

    private void sha256Operation(String[] instruction) throws StackUnderflowException, StackOverflowException, NoSuchAlgorithmException {
        if (this.argumentsAreMoreThan(instruction, 0)) {
            return;
        }

        Type first = stack.pop();

        if (!first.getType().equals("str")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        StrType firstStr = (StrType) first;

        // Compute the SHA256 hash
        Base64.Encoder encoder = Base64.getEncoder();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        StrType result = new StrType(encoder.encodeToString(digest.digest(firstStr.getValue().getBytes(StandardCharsets.UTF_8))));

        stack.push(result);
    }

    private void equalOperation(String[] instruction) throws StackUnderflowException {
        if (this.argumentsAreMoreThan(instruction, 0)) {
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals("str") && !second.getType().equals("str")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        StrType firstStr = (StrType) first;
        StrType secondStr = (StrType) second;

        if (!firstStr.getValue().equals(secondStr.getValue())) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE_OR_TYPE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }
    }

    private void checksigOperation(String[] instruction) throws Exception {
        if (this.argumentsAreMoreThan(instruction, 0)) {
            return;
        }

        Type second = stack.pop();  // Public key
        Type first = stack.pop();   // Signature

        if (!first.getType().equals("str") && !second.getType().equals("str")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        StrType signature = (StrType) first;
        StrType publicKey = (StrType) second;

        // Verify the signature
        BoolType result = new BoolType(verify(propertyId, signature.getValue(), getPublicKeyFromString(publicKey.getValue())));

        stack.push(result);
    }
}

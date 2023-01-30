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
            if (!trap.isEmptyStack()) {
                haltProgramExecution();
                break;
            }

            try {
                executionPointer++;
                String singleInstruction = this.instructions[executionPointer].trim();
                String[] instruction = singleInstruction.split(" ");

                if (!(instruction.length == 1 && instruction[0].endsWith(":"))) {
                    System.out.println("execute: instruction => " + instruction[0]);
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
            } catch (StackOverflowException | StackUnderflowException error) {
                trap.raiseError(error.getCode(), executionPointer);
            } catch (NoSuchAlgorithmException error) {
                System.out.println("execute: Error while executing the code\nError: " + error.getMessage());
                throw new RuntimeException(error);
            }
        }

        if (!trap.isEmptyStack()) {
            System.out.println("\nErrors in the stack");
            System.out.println(trap.printStack());
            return false;
        }

        if (stack.isEmpty()) {
            System.out.println("execute: Stack => The stack is empty");
            return false;
        }

        Type value = stack.pop();

        if (!stack.isEmpty()) {
            System.out.println("execute: Stack => There is more than one value in the stack");
            return false;
        }

        if (!value.getType().equals("bool")) {
            System.out.println("execute: Stack => The last value in the stack is not a boolean");
            return false;
        }

        BoolType boolVal = (BoolType) value;

        if (!boolVal.getValue()) {
            System.out.println("execute: Stack => The funds can't be use. The script can't be unlocked");
            return false;
        }

        System.out.println("Final state of the execution below");

        System.out.println("\nGlobal state of the execution" +
                "\nrunning -> " + isRunning +
                "\ni -> " + executionPointer +
                "\ni (with offset) -> " + executionPointer +
                "\nlength of the program -> " + instructions.length +
                "\nlength of the program (with offset) -> " + instructions.length);

        return !isRunning;
    }

    private void haltProgramExecution() {
        isRunning = false;
    }

    private void haltOperation(String[] instruction) {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        // Terminate the execution
        this.haltProgramExecution();
    }

    private void pushOperation(String[] instruction) throws StackOverflowException, NoSuchAlgorithmException {
        if ((instruction.length - 1) < 2) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if ((instruction.length - 1) > 2) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
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
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        Type first = stack.pop();
        stack.push(first);
        stack.push(first);
    }

    private void sha256Operation(String[] instruction) throws StackUnderflowException, StackOverflowException, NoSuchAlgorithmException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
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
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
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
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
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
        System.out.println("checksigOperation: stack => " + stack);
    }
}

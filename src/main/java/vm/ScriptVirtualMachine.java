package vm;

import exceptions.stack.StackOverflowException;
import exceptions.stack.StackUnderflowException;
import lib.datastructures.Stack;
import vm.trap.Trap;
import vm.trap.TrapErrorCodes;
import vm.types.*;
import vm.types.address.AddrType;
import vm.types.address.Address;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;

import static lib.crypto.Crypto.getPublicKeyFromString;
import static lib.crypto.Crypto.verify;

public class ScriptVirtualMachine {
    private final String[] instructions;
    private boolean isRunning = true;
    private int executionPointer = -1;
    private final Stack<Type> stack = new Stack<Type>(10);
    private final Trap trap;

    public ScriptVirtualMachine(String[] instructions) {
        this.instructions = instructions;
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
                            if ((instruction.length - 1) > 0) {
                                trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, instruction[0]);
                                break;
                            }
                            // Terminate the execution
                            this.haltProgramExecution();
                            break;
                        default:
                            trap.raiseError(TrapErrorCodes.INSTRUCTION_DOES_NOT_EXISTS, executionPointer, instruction[0]);
                    }
                }
            } catch (StackOverflowException | StackUnderflowException error) {
                trap.raiseError(error.getCode(), executionPointer);
            } catch (NoSuchAlgorithmException error) {
                System.out.println("execute: Error while executing the code\nError: " + error.getMessage());
                throw new RuntimeException(error);
            }
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

        if (!trap.isStackEmpty()) {
            System.out.println("\nErrors in the stack");
            System.out.println(trap.printStack());
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

    private void pushOperation(String[] instruction) throws StackOverflowException, NoSuchAlgorithmException {
        if ((instruction.length - 1) < 2) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 2) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, instruction[0]);
            return;
        }

        String type = instruction[1];
        String value = instruction[2];

        if (!type.equals("str")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, instruction[0]);
            return;
        }

        stack.push(new StrType(value));
    }

    private void dupOperation(String[] instruction) throws StackUnderflowException, StackOverflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, instruction[0]);
            return;
        }

        Type first = stack.pop();
        stack.push(first);
        stack.push(first);
    }

    private void sha256Operation(String[] instruction) throws StackUnderflowException, StackOverflowException, NoSuchAlgorithmException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, instruction[0]);
            return;
        }

        Type first = stack.pop();

        if (!first.getType().equals("str")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, instruction[0]);
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
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals("str") && !second.getType().equals("str")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, instruction[0]);
            return;
        }

        StrType firstStr = (StrType) first;
        StrType secondStr = (StrType) second;

        if (!firstStr.getValue().equals(secondStr.getValue())) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE_OR_TYPE_DOES_NOT_EXIST, executionPointer, instruction[0]);
            return;
        }
    }

    private void checksigOperation(String[] instruction) throws Exception {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, instruction[0]);
            return;
        }

        Type second = stack.pop();  // Public key
        Type first = stack.pop();   // Signature

        if (!first.getType().equals("str") && !second.getType().equals("str")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, instruction[0]);
            return;
        }

        StrType signature = (StrType) first;
        StrType publicKey = (StrType) second;

        // Verify the signature
        BoolType result = new BoolType(verify("aaa111", signature.getValue(), getPublicKeyFromString(publicKey.getValue())));

        stack.push(result);
    }
}

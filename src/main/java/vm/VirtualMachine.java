package vm;

import exceptions.stack.StackOverflowException;
import exceptions.stack.StackUnderflowException;
import lib.datastructures.Stack;
import vm.trap.Trap;
import vm.trap.TrapErrorCodes;
import vm.types.*;
import vm.types.address.AddrType;
import vm.types.address.Address;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Objects;

public class VirtualMachine {
    private final String[] instructions;
    private boolean isRunning = true;
    private int i = -1;
    private final int offset;
    private final Stack<Type> stack = new Stack<Type>(10);
    private final HashMap<String, Type> dataSpace = new HashMap<String, Type>();
    private final HashMap<String, Type> argumentsSpace = new HashMap<String, Type>();
    private final HashMap<String, TraceChange> globalSpace;
    private final Trap trap = new Trap();
    // private String stuffToStore; // TODO: data to save in a blockchain transaction
    // private int programCounter; // or instructionPointer
    // private int stackPointer;

    public VirtualMachine(String[] instructions, int offset) {
        this.instructions = instructions;
        this.offset = offset;
        this.globalSpace = new HashMap<String, TraceChange>();
    }

    public VirtualMachine(String[] instructions, int offset, HashMap<String, TraceChange> globalSpace) {
        this.instructions = instructions;
        this.offset = offset;
        this.globalSpace = globalSpace;
    }

    public HashMap<String, TraceChange> getGlobalSpace() {
        return globalSpace;
    }

    public boolean execute() {
        while (isRunning) {
            if (!trap.isStackEmpty()) {
                haltProgramExecution();
                break;
            }

            try {
                i++;
                String singleInstruction = this.instructions[i].trim();
                String[] instruction = singleInstruction.split(" ");

                if (!(instruction.length == 1 && instruction[0].endsWith(":"))) {
                    switch (instruction[0]) {
                        case "PUSH":
                            this.pushOperation(instruction);
                            break;
                        case "ADD":
                            this.addOperation(instruction);
                            break;
                        case "SUB":
                            this.subOperation(instruction);
                            break;
                        case "MUL":
                            this.mulOperation(instruction);
                            break;
                        case "DIV":
                            this.divOperation(instruction);
                            break;
                        case "INST":
                            this.instOperation(instruction);
                            break;
                        case "LOAD":
                            this.loadOperation(instruction);
                            break;
                        case "STORE":
                            this.storeOperation(instruction);
                            break;
                        case "JMP":
                            this.jmpOperation(instruction);
                            break;
                        case "JMPIF":
                            this.jmpifOperation(instruction);
                            break;
                        case "ISEQ":
                            this.iseqOperation(instruction);
                            break;
                        case "ISGE":
                            this.isgeOperation(instruction);
                            break;
                        case "ISGT":
                            this.isgtOperation(instruction);
                            break;
                        case "ISLE":
                            this.isleOperation(instruction);
                            break;
                        case "ISLT":
                            this.isltOperation(instruction);
                            break;
                        case "AINST":
                            this.ainstOperation(instruction);
                            break;
                        case "ASTORE":
                            this.astoreOperation(instruction);
                            break;
                        case "ALOAD":
                            this.aloadOperation(instruction);
                            break;
                        case "GINST":
                            this.ginstOperation(instruction);
                            break;
                        case "GSTORE":
                            this.gstoreOperation(instruction);
                            break;
                        case "GLOAD":
                            this.gloadOperation(instruction);
                            break;
                        case "HALT":
                            if ((instruction.length - 1) > 0) {
                                trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
                                break;
                            }

                            // Terminate the program
                            this.haltProgramExecution();
                            break;
                        default:
                            System.out.println("offset: " + offset);
                            trap.raiseError(TrapErrorCodes.INSTRUCTION_DOES_NOT_EXISTS, (i + 1), (i + 1 + offset), instruction[0]);
                    }
                }
            } catch (StackOverflowException | StackUnderflowException error) {
                trap.raiseError(error.getCode(), (i + 1), (i + 1 + offset));
            } catch (NoSuchAlgorithmException error) {
                System.out.println("execute: Error while executing the code\nError: " + error.getMessage());
                throw new RuntimeException(error);
            }
        }

        if (!trap.isStackEmpty()) {
            System.out.println("\nErrors in the stack");
            System.out.println(trap.printStack());
            return false;
        }

        System.out.println("Final state of the execution below");

        if (stack.isEmpty()) {
            System.out.println("Stack: The stack is empty");
        } else {
            System.out.println("Stack");
            while (!stack.isEmpty()) {
                try {
                    System.out.println("Value: " + stack.pop().getValue().toString());
                } catch (StackUnderflowException error) {
                    System.exit(-1);
                }
            }
        }

        if (globalSpace.isEmpty()) {
            System.out.println("\nGlobalSpace: The global space is empty");
        } else {
            System.out.println("\nGlobalSpace");
            for (HashMap.Entry<String, TraceChange> entry : globalSpace.entrySet()) {
                TraceChange value = entry.getValue();
                if (value.getValue().getType().equals("addr")) {
                    AddrType address = (AddrType) value.getValue();
                    System.out.println(entry.getKey() + ": " + address.getAddress() + " " + address.getPublicKey());
                } else {
                    System.out.println(entry.getKey() + ": " + value.getValue().getValue());
                }
            }
        }

        if (argumentsSpace.isEmpty()) {
            System.out.println("\nArgumentsSpace: The argument space is empty");
        } else {
            System.out.println("\nArgumentsSpace");
            for (HashMap.Entry<String, Type> entry : argumentsSpace.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue().getValue());
            }
        }

        if (dataSpace.isEmpty()) {
            System.out.println("\nDataSpace: The data space is empty");
        } else {
            System.out.println("\nDataSpace");
            for (HashMap.Entry<String, Type> entry : dataSpace.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue().getValue());
            }
        }

        System.out.println("\nGlobal state of the execution" +
                "\nrunning -> " + isRunning +
                "\ni -> " + i +
                "\ni (with offset) -> " + (i + offset) +
                "\nlength of the program -> " + instructions.length +
                "\nlength of the program (with offset) -> " + (instructions.length + offset));

        return !isRunning;
    }

    private void haltProgramExecution() {
        isRunning = false;
    }

    // Instructions

    private void pushOperation(String[] instruction) throws StackOverflowException, NoSuchAlgorithmException {
        if ((instruction.length - 1) < 2) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 2 && !instruction[1].equals("float")) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        if (instruction.length - 1 > 3) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        String type = instruction[1];
        String value = instruction[2];
        String decimals = "";
        if (type.equals("float")) {
            decimals = instruction[3];
        }

        switch (type) {
            case "int":
                stack.push(new IntType(Integer.parseInt(value)));
                break;
            case "bool":
                stack.push(new BoolType(Boolean.parseBoolean(value)));
                break;
            case "str":
                stack.push(new StrType(value));
                break;
            case "addr":
                stack.push(new AddrType(new Address(value)));
                break;
            case "float":
                stack.push(new FloatType(Integer.parseInt(value), Integer.parseInt(decimals)));
                break;
            default:
                trap.raiseError(TrapErrorCodes.TYPE_DOES_NOT_EXIST, (i + 1), (i + 1 + offset));
        }
    }

    private void addOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();
        Type result;

        if (first.getType().equals("int") && second.getType().equals("int")) {
            IntType firstVal = (IntType) first;
            IntType secondVal = (IntType) second;
            result = new IntType(firstVal.getValue() + secondVal.getValue());
        } else if (first.getType().equals("float") && second.getType().equals("float")) {
            FloatType firstVal = (FloatType) first;
            FloatType secondVal = (FloatType) second;
            result = new FloatType(firstVal.getInteger() + secondVal.getInteger(), firstVal.getDecimals());
        } else {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset), "ADD");
            return;
        }
        stack.push(result);
    }

    private void subOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();
        Type result;

        if (first.getType().equals("int") && second.getType().equals("int")) {
            IntType firstVal = (IntType) first;
            IntType secondVal = (IntType) second;
            result = new IntType(firstVal.getValue() - secondVal.getValue());
        } else if (first.getType().equals("float") && second.getType().equals("float")) {
            FloatType firstVal = (FloatType) first;
            FloatType secondVal = (FloatType) second;
            result = new FloatType(firstVal.getInteger() - secondVal.getInteger(), firstVal.getDecimals());
        } else {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset), "ADD");
            return;
        }
        stack.push(result);
    }

    private void mulOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();
        Type result;

        if (first.getType().equals("int") && second.getType().equals("int")) {
            IntType firstVal = (IntType) first;
            IntType secondVal = (IntType) second;
            result = new IntType(firstVal.getValue() * secondVal.getValue());
        } else if (first.getType().equals("float") && second.getType().equals("float")) {
            FloatType firstVal = (FloatType) first;
            FloatType secondVal = (FloatType) second;
            result = new FloatType(firstVal.getInteger() * secondVal.getInteger(), firstVal.getDecimals());
        } else {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset), "ADD");
            return;
        }
        stack.push(result);
    }

    private void divOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();
        Type result;

        if (first.getType().equals("int") && second.getType().equals("int")) {
            IntType firstVal = (IntType) first;
            IntType secondVal = (IntType) second;
            result = new FloatType(firstVal.getValue() / secondVal.getValue(), 2);
        } else if (first.getType().equals("float") && second.getType().equals("float")) {
            FloatType firstVal = (FloatType) first;
            FloatType secondVal = (FloatType) second;
            result = new FloatType(firstVal.getInteger() / secondVal.getInteger(), firstVal.getDecimals());
        } else {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset), "ADD");
            return;
        }
        stack.push(result);
    }

    private void instOperation(String[] instruction) {
        if ((instruction.length - 1) < 2) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 2 && !instruction[1].equals("float")) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        if (instruction.length - 1 > 3) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        String type = instruction[1];
        String variableName = instruction[2];
        String decimals = "";
        if (type.equals("float")) {
            decimals = instruction[3];
        }

        if (dataSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_ALREADY_EXIST, (i + 1), (i + 1 + offset));
        }

        switch (type) {
            case "int":
                dataSpace.put(variableName, new IntType());
                break;
            case "bool":
                dataSpace.put(variableName, new BoolType());
                break;
            case "str":
                dataSpace.put(variableName, new StrType());
                break;
            case "addr":
                dataSpace.put(variableName, new AddrType());
                break;
            case "float":
                dataSpace.put(variableName, new FloatType(0, Integer.parseInt(decimals)));
                break;
            default:
                trap.raiseError(TrapErrorCodes.TYPE_DOES_NOT_EXIST, (i + 1), (i + 1 + offset));
        }
    }

    private void loadOperation(String[] instruction) throws StackOverflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        String variableName = instruction[1];

        if (!dataSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, (i + 1), (i + 1 + offset));
        }
        stack.push(dataSpace.get(variableName));
    }

    private void storeOperation(String[] instruction) throws StackUnderflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        String variableName = instruction[1];

        if (!dataSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_ALREADY_EXIST, (i + 1), (i + 1 + offset));
        }
        Type value = stack.pop();
        dataSpace.put(variableName, value);
    }

    private void jmpOperation(String[] instruction) {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        String label = instruction[1];

        int j = i;
        boolean found = false;

        while (!found && j < this.instructions.length) {
            if (this.instructions[j].trim().equals(label + ":")) {
                found = true;
            }
            j++;
        }

        if (found) {
            i = j - 1;
        } else {
            trap.raiseError(TrapErrorCodes.LABEL_DOES_NOT_EXISTS, (i + 1), (i + 1 + offset));
        }
    }

    private void jmpifOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        Type resultOfEvaluation = stack.pop();

        if (!resultOfEvaluation.getType().equals("bool")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset));
            return;
        }

        BoolType resultOfEvaluationVal = new BoolType((Boolean) resultOfEvaluation.getValue());

        if (resultOfEvaluationVal.getValue()) {
            jmpOperation(instruction);
        } else {
            stack.push(resultOfEvaluation);
        }
    }

    private void iseqOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals(second.getType())) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset));
            return;
        }

        switch (first.getType()) {
            case "int":
                IntType firstInt = new IntType((Integer) first.getValue());
                IntType secondInt = new IntType((Integer) second.getValue());
                stack.push(new BoolType(firstInt.getValue() == secondInt.getValue()));
                break;
            case "bool":
                BoolType firstBool = new BoolType((Boolean) first.getValue());
                BoolType secondBool = new BoolType((Boolean) second.getValue());
                stack.push(new BoolType(firstBool.getValue() == secondBool.getValue()));
                break;
            case "str":
                StrType firstStr = new StrType((String) first.getValue());
                StrType secondStr = new StrType((String) second.getValue());
                stack.push(new BoolType(firstStr.getValue().equals(secondStr.getValue())));
                break;
            case "addr":
                AddrType firstAddr = new AddrType((Address) first.getValue());
                AddrType secondAddr = new AddrType((Address) second.getValue());
                stack.push(new BoolType(firstAddr.getValue().equals(secondAddr.getValue())));
                break;
            case "float":
                FloatType firstFloat = (FloatType) first;
                FloatType secondFloat = (FloatType) second;
                stack.push(new BoolType(Objects.equals(firstFloat.getValue(), secondFloat.getValue())));
                break;
            default:
                trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset));
        }
    }

    private void isgtOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals(second.getType())) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset));
            return;
        }

        if (!first.getType().equals("int") && !first.getType().equals("float")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset));
            return;
        }

        switch (first.getType()) {
            case "int":
                IntType firstVal = new IntType((Integer) first.getValue());
                IntType secondVal = new IntType((Integer) second.getValue());
                stack.push(new BoolType(firstVal.getValue() > secondVal.getValue()));
                break;
            case "float":
                FloatType firstFloat = (FloatType) first;
                FloatType secondFloat = (FloatType) second;
                stack.push(new BoolType(firstFloat.getValue().compareTo(secondFloat.getValue()) > 0));
                break;
            default:
                trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset));
        }
    }

    private void isgeOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals(second.getType())) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset));
            return;
        }

        if (!first.getType().equals("int") && !first.getType().equals("float")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset));
            return;
        }

        switch (first.getType()) {
            case "int":
                IntType firstVal = new IntType((Integer) first.getValue());
                IntType secondVal = new IntType((Integer) second.getValue());
                stack.push(new BoolType(firstVal.getValue() >= secondVal.getValue()));
                break;
            case "float":
                FloatType firstFloat = (FloatType) first;
                FloatType secondFloat = (FloatType) second;
                stack.push(new BoolType(firstFloat.getValue().compareTo(secondFloat.getValue()) >= 0));
                break;
            default:
                trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset));
        }
    }

    private void isltOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals(second.getType())) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset));
            return;
        }

        if (!first.getType().equals("int") && !first.getType().equals("float")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset));
            return;
        }

        switch (first.getType()) {
            case "int":
                IntType firstVal = new IntType((Integer) first.getValue());
                IntType secondVal = new IntType((Integer) second.getValue());
                stack.push(new BoolType(firstVal.getValue() < secondVal.getValue()));
                break;
            case "float":
                FloatType firstFloat = (FloatType) first;
                FloatType secondFloat = (FloatType) second;
                stack.push(new BoolType(firstFloat.getValue().compareTo(secondFloat.getValue()) < 0));
                break;
            default:
                trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset));
        }
    }

    private void isleOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals(second.getType())) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset));
            return;
        }

        if (!first.getType().equals("int") && !first.getType().equals("float")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset));
            return;
        }

        switch (first.getType()) {
            case "int":
                IntType firstVal = new IntType((Integer) first.getValue());
                IntType secondVal = new IntType((Integer) second.getValue());
                stack.push(new BoolType(firstVal.getValue() <= secondVal.getValue()));
                break;
            case "float":
                FloatType firstFloat = (FloatType) first;
                FloatType secondFloat = (FloatType) second;
                stack.push(new BoolType(firstFloat.getValue().compareTo(secondFloat.getValue()) <= 0));
                break;
            default:
                trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1), (i + 1 + offset));
        }
    }

    private void ainstOperation(String[] instruction) {
        if ((instruction.length - 1) < 2) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 2 && !instruction[1].equals("float")) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        if (instruction.length - 1 > 3) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        String type = instruction[1];
        String variableName = instruction[2];
        String decimals = "";
        if (type.equals("float")) {
            decimals = instruction[3];
        }

        if (argumentsSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_ALREADY_EXIST, (i + 1), (i + 1 + offset));
        }

        switch (type) {
            case "int":
                argumentsSpace.put(variableName, new IntType());
                break;
            case "bool":
                argumentsSpace.put(variableName, new BoolType());
                break;
            case "str":
                argumentsSpace.put(variableName, new StrType());
                break;
            case "addr":
                argumentsSpace.put(variableName, new AddrType());
                break;
            case "float":
                argumentsSpace.put(variableName, new FloatType(0, Integer.parseInt(decimals)));
                break;
            default:
                trap.raiseError(TrapErrorCodes.TYPE_DOES_NOT_EXIST, (i + 1), (i + 1 + offset));
        }
    }

    private void aloadOperation(String[] instruction) throws StackOverflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        String variableName = instruction[1];

        if (!argumentsSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, (i + 1), (i + 1 + offset));
        }
        stack.push(argumentsSpace.get(variableName));
    }

    private void astoreOperation(String[] instruction) throws StackUnderflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        String variableName = instruction[1];

        if (!argumentsSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_ALREADY_EXIST, (i + 1), (i + 1 + offset));
        }
        Type value = stack.pop();
        argumentsSpace.put(variableName, value);
    }

    private void ginstOperation(String[] instruction) {
        if ((instruction.length - 1) < 2) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 2 && !instruction[1].equals("float")) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        if (instruction.length - 1 > 3) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        String type = instruction[1];
        String variableName = instruction[2];
        String decimals = "";
        if (type.equals("float")) {
            decimals = instruction[3];
        }

        if (globalSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_ALREADY_EXIST, (i + 1), (i + 1 + offset));
            return;
        }

        switch (type) {
            case "int":
                globalSpace.put(variableName, new TraceChange(new IntType(), true));
                break;
            case "bool":
                globalSpace.put(variableName, new TraceChange(new BoolType(), true));
                break;
            case "str":
                globalSpace.put(variableName, new TraceChange(new StrType(), true));
                break;
            case "addr":
                globalSpace.put(variableName, new TraceChange(new AddrType(), true));
                break;
            case "float":
                globalSpace.put(variableName, new TraceChange(new FloatType(0, Integer.parseInt(decimals)), true));
                break;
            default:
                trap.raiseError(TrapErrorCodes.TYPE_DOES_NOT_EXIST, (i + 1), (i + 1 + offset));
        }
    }

    private void gloadOperation(String[] instruction) throws StackOverflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        String variableName = instruction[1];

        if (!globalSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, (i + 1), (i + 1 + offset));
        }
        stack.push(globalSpace.get(variableName).getValue());
    }

    private void gstoreOperation(String[] instruction) throws StackUnderflowException, NoSuchAlgorithmException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), (i + 1 + offset), instruction[0]);
            return;
        }

        String variableName = instruction[1];

        if (!globalSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_ALREADY_EXIST, (i + 1), (i + 1 + offset));
        }

        Type value = stack.pop();
        if (value.getType().equals("addr")) {
            AddrType address = (AddrType) value;
            globalSpace.put(variableName, new TraceChange(new AddrType(new Address(address.getPublicKey())), true));
        } else {
            globalSpace.put(variableName, new TraceChange(value, true));
        }

    }
}

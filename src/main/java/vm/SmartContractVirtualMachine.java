package vm;

import models.assets.FungibleAsset;
import exceptions.stack.StackOverflowException;
import exceptions.stack.StackUnderflowException;
import lib.datastructures.Stack;
import models.contract.SingleUseSeal;
import vm.trap.Trap;
import vm.trap.TrapErrorCodes;
import vm.types.*;
import vm.types.address.AddrType;
import vm.types.address.Address;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class SmartContractVirtualMachine {
    private final String[] instructions;
    private boolean isRunning = true;
    private int executionPointer = -1;
    private final int offset;
    private final Stack<Type> stack = new Stack<Type>(10);
    private final HashMap<String, Type> dataSpace = new HashMap<String, Type>();
    private final HashMap<String, Type> argumentsSpace = new HashMap<String, Type>();
    private final HashMap<String, TraceChange> globalSpace;
    private final ArrayList<SingleUseSeal> singleUseSealsToSend = new ArrayList<>();
    private final Trap trap;
    // private String stuffToStore; // TODO: data to save in a blockchain transaction
    // private int programCounter; // or instructionPointer
    // private int stackPointer;

    public SmartContractVirtualMachine(String[] instructions, int offset) {
        this.instructions = instructions;
        this.offset = offset;
        this.globalSpace = new HashMap<String, TraceChange>();
        this.trap = new Trap(offset);
    }

    public SmartContractVirtualMachine(String[] instructions, int offset, HashMap<String, TraceChange> globalSpace) {
        this.instructions = instructions;
        this.offset = offset;
        this.globalSpace = globalSpace;
        this.trap = new Trap(offset);
    }

    public HashMap<String, TraceChange> getGlobalSpace() {
        return globalSpace;
    }

    public ArrayList<SingleUseSeal> getSingleUseSealsToSend() {
        return singleUseSealsToSend;
    }

    public boolean execute() {
        while (isRunning) {
            if (trap.isEmptyStack()) {
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
                        case "AND":
                            this.andOperation(instruction);
                            break;
                        case "OR":
                            this.orOperation(instruction);
                            break;
                        case "NOT":
                            this.notOperation(instruction);
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
                        case "DEPOSIT":
                            this.depositOperation(instruction);
                            break;
                        case "WITHDRAW":
                            this.withdrawOperation(instruction);
                            break;
                        case "RAISE":
                            this.raiseOperation(instruction);
                            break;
                        case "HALT":
                            if ((instruction.length - 1) > 0) {
                                trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
                                break;
                            }
                            // Terminate the execution
                            this.haltProgramExecution();
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

        if (trap.isEmptyStack()) {
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
                    System.out.println(entry.getKey() + ": " + address.getAddress() + " " + address.getPublicKey() + ", changed: " + value.isChanged());
                } else if (value.getValue().getType().equals("asset")) {
                    AssetType asset = (AssetType) value.getValue();
                    System.out.println(entry.getKey() + ": " + asset.getValue().getValue() + " " + asset.getAssetId() + ", changed: " + value.isChanged());
                } else {
                    System.out.println(entry.getKey() + ": " + value.getValue().getValue() + ", changed: " + value.isChanged());
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
                "\ni -> " + executionPointer +
                "\ni (with offset) -> " + (executionPointer + offset) +
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
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if ((instruction.length - 1) > 2 && !instruction[1].equals("float")) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (instruction.length - 1 > 3) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
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
                trap.raiseError(TrapErrorCodes.TYPE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
        }
    }

    private void addOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
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
        } else if (first.getType().equals("asset") && second.getType().equals("asset")) {
            AssetType firstVal = (AssetType) first;
            AssetType secondVal = (AssetType) second;

            if (!firstVal.getAssetId().equals(secondVal.getAssetId())) {
                trap.raiseError(TrapErrorCodes.ASSET_IDS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            if (firstVal.getValue().getDecimals() != secondVal.getValue().getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new FloatType(
                    firstVal.getValue().getInteger() + secondVal.getValue().getInteger(),
                    firstVal.getValue().getDecimals()
            );
        } else if (first.getType().equals("asset") && second.getType().equals("float")) {
            AssetType firstVal = (AssetType) first;
            FloatType secondVal = (FloatType) second;

            if (firstVal.getValue().getDecimals() != secondVal.getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new FloatType(
                    firstVal.getValue().getInteger() + secondVal.getInteger(),
                    firstVal.getValue().getDecimals()
            );
        } else if (first.getType().equals("float") && second.getType().equals("asset")) {
            FloatType firstVal = (FloatType) first;
            AssetType secondVal = (AssetType) second;

            if (firstVal.getDecimals() != secondVal.getValue().getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new FloatType(
                    firstVal.getInteger() + secondVal.getValue().getInteger(),
                    firstVal.getDecimals()
            );
        } else {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }
        stack.push(result);
    }

    private void subOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
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
        } else if (first.getType().equals("asset") && second.getType().equals("asset")) {
            AssetType firstVal = (AssetType) first;
            AssetType secondVal = (AssetType) second;

            if (!firstVal.getAssetId().equals(secondVal.getAssetId())) {
                trap.raiseError(TrapErrorCodes.ASSET_IDS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            if (firstVal.getValue().getDecimals() != secondVal.getValue().getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new FloatType(
                    firstVal.getValue().getInteger() - secondVal.getValue().getInteger(),
                    firstVal.getValue().getDecimals()
            );
        } else if (first.getType().equals("asset") && second.getType().equals("float")) {
            AssetType firstVal = (AssetType) first;
            FloatType secondVal = (FloatType) second;

            if (firstVal.getValue().getDecimals() != secondVal.getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new FloatType(
                    firstVal.getValue().getInteger() - secondVal.getInteger(),
                    firstVal.getValue().getDecimals()
            );
        } else if (first.getType().equals("float") && second.getType().equals("asset")) {
            FloatType firstVal = (FloatType) first;
            AssetType secondVal = (AssetType) second;

            if (firstVal.getDecimals() != secondVal.getValue().getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new FloatType(
                    firstVal.getInteger() - secondVal.getValue().getInteger(),
                    firstVal.getDecimals()
            );
        } else {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }
        stack.push(result);
    }

    private void mulOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
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
            result = new FloatType(
                    (new Double(
                            (firstVal.getInteger() * secondVal.getInteger()) / Math.pow(10, firstVal.getDecimals()))
                    ).intValue(),
                    firstVal.getDecimals()
            );
        } else if (first.getType().equals("asset") && second.getType().equals("asset")) {
            AssetType firstVal = (AssetType) first;
            AssetType secondVal = (AssetType) second;

            if (!firstVal.getAssetId().equals(secondVal.getAssetId())) {
                trap.raiseError(TrapErrorCodes.ASSET_IDS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            if (firstVal.getValue().getDecimals() != secondVal.getValue().getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new FloatType(
                    (new Double(
                            (firstVal.getValue().getInteger() * secondVal.getValue().getInteger()) / Math.pow(10, firstVal.getValue().getDecimals()))
                    ).intValue(),
                    firstVal.getValue().getDecimals()
            );
        } else if (first.getType().equals("asset") && second.getType().equals("float")) {
            AssetType firstVal = (AssetType) first;
            FloatType secondVal = (FloatType) second;

            if (firstVal.getValue().getDecimals() != secondVal.getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new FloatType(
                    (new Double(
                            (firstVal.getValue().getInteger() * secondVal.getInteger()) / Math.pow(10, secondVal.getDecimals()))
                    ).intValue(),
                    firstVal.getValue().getDecimals()
            );
        } else if (first.getType().equals("float") && second.getType().equals("asset")) {
            FloatType firstVal = (FloatType) first;
            AssetType secondVal = (AssetType) second;

            if (firstVal.getDecimals() != secondVal.getValue().getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new FloatType(
                    (new Double(
                            (firstVal.getInteger() * secondVal.getValue().getInteger()) / Math.pow(10, firstVal.getDecimals()))
                    ).intValue(),
                    firstVal.getDecimals()
            );
        } else {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }
        stack.push(result);
    }

    private void divOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();
        Type result;

        if (first.getType().equals("int") && second.getType().equals("int")) {
            IntType firstVal = (IntType) first;
            IntType secondVal = (IntType) second;

            // Check if the denominator is zero
            if (secondVal.getValue() == 0) {
                trap.raiseError(TrapErrorCodes.DIVISION_BY_ZERO, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new FloatType(firstVal.getValue() / secondVal.getValue(), 2);
        } else if (first.getType().equals("float") && second.getType().equals("float")) {
            FloatType firstVal = (FloatType) first;
            FloatType secondVal = (FloatType) second;

            System.out.println(firstVal);
            System.out.println(secondVal);

            if (firstVal.getDecimals() != secondVal.getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            // Check if the denominator is zero
            if (secondVal.getInteger() == 0) {
                trap.raiseError(TrapErrorCodes.DIVISION_BY_ZERO, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new FloatType(
                    (firstVal.getValue().divide(secondVal.getValue(), RoundingMode.CEILING))
                            .multiply(BigDecimal.valueOf(Math.pow(10, firstVal.getDecimals())))
                            .intValue(),
                    firstVal.getDecimals()
            );
        } else if (first.getType().equals("asset") && second.getType().equals("asset")) {
            AssetType firstVal = (AssetType) first;
            AssetType secondVal = (AssetType) second;

            if (!firstVal.getAssetId().equals(secondVal.getAssetId())) {
                trap.raiseError(TrapErrorCodes.ASSET_IDS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            if (firstVal.getValue().getDecimals() != secondVal.getValue().getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            // Check if the denominator is zero
            if (secondVal.getValue().getInteger() == 0) {
                trap.raiseError(TrapErrorCodes.DIVISION_BY_ZERO, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new FloatType(
                    (firstVal.getValue().getValue().divide(secondVal.getValue().getValue(), RoundingMode.CEILING))
                            .multiply(BigDecimal.valueOf(Math.pow(10, firstVal.getValue().getDecimals())))
                            .intValue(),
                    firstVal.getValue().getDecimals()
            );
        } else if (first.getType().equals("asset") && second.getType().equals("float")) {
            AssetType firstVal = (AssetType) first;
            FloatType secondVal = (FloatType) second;

            if (firstVal.getValue().getDecimals() != secondVal.getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            // Check if the denominator is zero
            if (secondVal.getInteger() == 0) {
                trap.raiseError(TrapErrorCodes.DIVISION_BY_ZERO, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new FloatType(
                    (firstVal.getValue().getValue().divide(secondVal.getValue(), RoundingMode.CEILING))
                            .multiply(BigDecimal.valueOf(Math.pow(10, firstVal.getValue().getDecimals())))
                            .intValue(),
                    firstVal.getValue().getDecimals()
            );
        } else if (first.getType().equals("float") && second.getType().equals("asset")) {
            FloatType firstVal = (FloatType) first;
            AssetType secondVal = (AssetType) second;

            if (firstVal.getDecimals() != secondVal.getValue().getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            // Check if the denominator is zero
            if (secondVal.getValue().getInteger() == 0) {
                trap.raiseError(TrapErrorCodes.DIVISION_BY_ZERO, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new FloatType(
                    (firstVal.getValue().divide(secondVal.getValue().getValue(), RoundingMode.CEILING))
                            .multiply(BigDecimal.valueOf(Math.pow(10, firstVal.getDecimals())))
                            .intValue(),
                    firstVal.getDecimals()
            );
        } else {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }
        stack.push(result);
    }

    private void instOperation(String[] instruction) {
        if ((instruction.length - 1) < 2) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if ((instruction.length - 1) > 2 && !instruction[1].equals("float")) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (instruction.length - 1 > 3) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        String type = instruction[1];
        String variableName = instruction[2];
        String decimals = "";
        if (type.equals("float")) {
            decimals = instruction[3];
        }

        if (dataSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_ALREADY_EXIST, executionPointer, Arrays.toString(instruction));
            return;
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
                trap.raiseError(TrapErrorCodes.TYPE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
        }
    }

    private void loadOperation(String[] instruction) throws StackOverflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        String variableName = instruction[1];

        if (!dataSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }
        stack.push(dataSpace.get(variableName));
    }

    private void storeOperation(String[] instruction) throws StackUnderflowException, NoSuchAlgorithmException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        String variableName = instruction[1];

        if (!dataSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }

        Type value = stack.pop();

        if (value.getType().equals("addr")) {
            AddrType address = (AddrType) value;
            dataSpace.put(variableName, new AddrType(new Address(address.getPublicKey())));
        } else {
            dataSpace.put(variableName, value);
        }
    }

    private void andOperation(String[] instruction) throws StackUnderflowException, StackOverflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals(second.getType())) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE_OR_TYPE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (!first.getType().equals("bool")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        BoolType firstBool = (BoolType) first;
        BoolType secondBool = (BoolType) second;
        BoolType result = new BoolType(firstBool.getValue() && secondBool.getValue());

        stack.push(result);
    }

    private void orOperation(String[] instruction) throws StackUnderflowException, StackOverflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals(second.getType())) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE_OR_TYPE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (!first.getType().equals("bool")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        BoolType firstBool = (BoolType) first;
        BoolType secondBool = (BoolType) second;
        BoolType result = new BoolType(firstBool.getValue() || secondBool.getValue());

        stack.push(result);
    }

    private void notOperation(String[] instruction) throws StackUnderflowException, StackOverflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        Type first = stack.pop();

        if (!first.getType().equals("bool")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        BoolType firstBool = (BoolType) first;
        BoolType result = new BoolType(!firstBool.getValue());

        stack.push(result);
    }

    private void jmpOperation(String[] instruction) {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        String label = instruction[1];

        int j = executionPointer;
        boolean found = false;

        while (!found && j < this.instructions.length) {
            if (this.instructions[j].trim().equals(label + ":")) {
                found = true;
            }
            j++;
        }

        if (found) {
            executionPointer = j - 1;
        } else {
            trap.raiseError(TrapErrorCodes.LABEL_DOES_NOT_EXISTS, executionPointer, Arrays.toString(instruction));
        }
    }

    private void jmpifOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        Type resultOfEvaluation = stack.pop();

        if (!resultOfEvaluation.getType().equals("bool")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
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
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (first.getType().equals("asset") && second.getType().equals("float")) {
            AssetType firstAsset = (AssetType) first;
            FloatType secondFloat = (FloatType) second;
            stack.push(new BoolType(Objects.equals(firstAsset.getValue().getValue(), secondFloat.getValue())));
            return;
        } else if (first.getType().equals("float") && second.getType().equals("asset")) {
            FloatType firstFloat = (FloatType) first;
            AssetType secondAsset = (AssetType) second;
            stack.push(new BoolType(Objects.equals(firstFloat.getValue(), secondAsset.getValue().getValue())));
            return;
        }

        if (!first.getType().equals(second.getType())) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE_OR_TYPE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
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
            case "asset":
                AssetType firstAsset = (AssetType) first;
                AssetType secondAsset = (AssetType) second;
                stack.push(new BoolType(Objects.equals(firstAsset.getValue().getValue(), secondAsset.getValue().getValue())));
                break;
            default:
                trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
        }
    }

    private void isltOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals("int") && !first.getType().equals("float") && !first.getType().equals("asset")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (!second.getType().equals("int") && !second.getType().equals("float") && !second.getType().equals("asset")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (first.getType().equals("asset") && second.getType().equals("float")) {
            AssetType firstAsset = (AssetType) first;
            FloatType secondFloat = (FloatType) second;
            stack.push(new BoolType(firstAsset.getValue().getValue().compareTo(secondFloat.getValue()) < 0));
            return;
        } else if (first.getType().equals("float") && second.getType().equals("asset")) {
            FloatType firstFloat = (FloatType) first;
            AssetType secondAsset = (AssetType) second;
            stack.push(new BoolType(firstFloat.getValue().compareTo(secondAsset.getValue().getValue()) < 0));
            return;
        }

        if (!first.getType().equals(second.getType())) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE_OR_TYPE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }

        switch (first.getType()) {
            case "int":
                IntType firstInt = new IntType((Integer) first.getValue());
                IntType secondInt = new IntType((Integer) second.getValue());
                stack.push(new BoolType(firstInt.getValue() < secondInt.getValue()));
                break;
            case "float":
                FloatType firstFloat = (FloatType) first;
                FloatType secondFloat = (FloatType) second;
                stack.push(new BoolType(firstFloat.getValue().compareTo(secondFloat.getValue()) < 0));
                break;
            case "asset":
                AssetType firstAsset = (AssetType) first;
                AssetType secondAsset = (AssetType) second;
                stack.push(new BoolType(firstAsset.getValue().getValue().compareTo(secondAsset.getValue().getValue()) < 0));
                break;
            default:
                trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
        }
    }

    private void isleOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals("int") && !first.getType().equals("float") && !first.getType().equals("asset")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (!second.getType().equals("int") && !second.getType().equals("float") && !second.getType().equals("asset")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (first.getType().equals("asset") && second.getType().equals("float")) {
            AssetType firstAsset = (AssetType) first;
            FloatType secondFloat = (FloatType) second;
            stack.push(new BoolType(firstAsset.getValue().getValue().compareTo(secondFloat.getValue()) <= 0));
            return;
        } else if (first.getType().equals("float") && second.getType().equals("asset")) {
            FloatType firstFloat = (FloatType) first;
            AssetType secondAsset = (AssetType) second;
            stack.push(new BoolType(firstFloat.getValue().compareTo(secondAsset.getValue().getValue()) <= 0));
            return;
        }

        if (!first.getType().equals(second.getType())) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE_OR_TYPE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
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
                trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
        }
    }

    private void ainstOperation(String[] instruction) {
        if ((instruction.length - 1) < 2) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if ((instruction.length - 1) > 2 && !instruction[1].equals("float") && !instruction[1].equals("asset")) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (instruction.length - 1 > 3) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        String type = instruction[1];
        String variableName = instruction[2];
        String decimals = "";
        String assetId = "";

        if (type.equals("float")) {
            decimals = instruction[3];
        } else if (type.equals("asset")) {
            assetId = instruction[3];
        }

        if (argumentsSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_ALREADY_EXIST, executionPointer, Arrays.toString(instruction));
            return;
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
            case "asset":
                FungibleAsset bitcoin = new FungibleAsset("iop890", "Bitcoin", "BTC", 10000, 2);

                if (assetId.equals(bitcoin.getAssetId())) {
                    AssetType value = new AssetType(bitcoin.getAssetId(), new FloatType(0, bitcoin.getDecimals()));
                    argumentsSpace.put(variableName, value);
                } else {
                    trap.raiseError(TrapErrorCodes.ASSET_IDS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                    return;
                }
                break;
            default:
                trap.raiseError(TrapErrorCodes.TYPE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
        }
    }

    private void aloadOperation(String[] instruction) throws StackOverflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        String variableName = instruction[1];

        if (!argumentsSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }

        stack.push(argumentsSpace.get(variableName));
    }

    private void astoreOperation(String[] instruction) throws StackUnderflowException, NoSuchAlgorithmException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        String variableName = instruction[1];

        if (!argumentsSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }

        Type currentValue = argumentsSpace.get(variableName);

        if (currentValue.getType().equals("asset")) {
            AssetType currentAssetValue = (AssetType) currentValue;

            Type value = stack.pop();

            // Check that the element popped from the stack is a float value
            if (!value.getType().equals("float")) {
                trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
                return;
            }

            FloatType floatValue = (FloatType) value;

            // Check that the element popped from the stack has the same decimals of the asset
            if (floatValue.getDecimals() != currentAssetValue.getValue().getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            AssetType newValue = new AssetType(
                    currentAssetValue.getAssetId(),
                    new FloatType(
                            floatValue.getInteger(),
                            floatValue.getDecimals()
                    )
            );
            argumentsSpace.put(variableName, newValue);
        } else {
            Type value = stack.pop();

            if (value.getType().equals("addr")) {
                AddrType address = (AddrType) value;
                argumentsSpace.put(variableName, new AddrType(new Address(address.getPublicKey())));
            } else {
                argumentsSpace.put(variableName, value);
            }
        }
    }

    private void ginstOperation(String[] instruction) {
        if ((instruction.length - 1) < 2) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if ((instruction.length - 1) > 2 && !instruction[1].equals("float") && !instruction[1].equals("asset")) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (instruction.length - 1 > 3) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        String type = instruction[1];
        String variableName = instruction[2];
        String decimals = "";
        String assetId = "";

        if (type.equals("float")) {
            decimals = instruction[3];
        } else if (type.equals("asset")) {
            assetId = instruction[3];
        }

        if (globalSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_ALREADY_EXIST, executionPointer, Arrays.toString(instruction));
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
            case "asset":
                FungibleAsset bitcoin = new FungibleAsset("iop890", "Bitcoin", "BTC", 10000, 2);

                if (assetId.equals(bitcoin.getAssetId())) {
                    AssetType value = new AssetType(bitcoin.getAssetId(), new FloatType(0, bitcoin.getDecimals()));
                    globalSpace.put(variableName, new TraceChange(value, true));
                } else {
                    trap.raiseError(TrapErrorCodes.ASSET_IDS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                    return;
                }
                break;
            default:
                trap.raiseError(TrapErrorCodes.TYPE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
        }
    }

    private void gloadOperation(String[] instruction) throws StackOverflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        String variableName = instruction[1];

        if (!globalSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }

        stack.push(globalSpace.get(variableName).getValue());
    }

    private void gstoreOperation(String[] instruction) throws StackUnderflowException, NoSuchAlgorithmException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        String variableName = instruction[1];

        if (!globalSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }

        Type value = stack.pop();

        if (value.getType().equals("asset")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (value.getType().equals("addr")) {
            AddrType address = (AddrType) value;
            globalSpace.put(variableName, new TraceChange(new AddrType(new Address(address.getPublicKey())), true));
        } else {
            globalSpace.put(variableName, new TraceChange(value, true));
        }
    }

    private void depositOperation(String[] instruction) throws StackUnderflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        String variableName = instruction[1];

        if (!globalSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }

        Type second = stack.pop();  // Asset
        Type first = stack.pop();   // Asset
        AssetType result;

        if (!first.getType().equals("asset") || !second.getType().equals("asset")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE_OR_TYPE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }

        AssetType assetToDeposit = (AssetType) first;
        AssetType assetContract = (AssetType) second;

        if (assetContract.getValue().getDecimals() != assetToDeposit.getValue().getDecimals()) {
            trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
            return;
        }

        // Check that the value of the asset to deposit is not negative
        if (assetToDeposit.getValue().getInteger() < 0) {
            trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
            return;
        }

        result = new AssetType(
                assetContract.getAssetId(),
                new FloatType(
                        assetContract.getValue().getInteger() + assetToDeposit.getValue().getInteger(),
                        assetToDeposit.getValue().getDecimals()
                )
        );

        globalSpace.put(variableName, new TraceChange(result, true));
    }

    private void withdrawOperation(String[] instruction) throws StackUnderflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        String variableName = instruction[1];

        if (!globalSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }

        Type third = stack.pop();   // Address
        Type second = stack.pop();  // Asset
        Type first = stack.pop();   // Float
        AssetType result;

        if (!first.getType().equals("float") || !second.getType().equals("asset") || !third.getType().equals("addr")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        FloatType floatVal = (FloatType) first;
        AssetType assetVal = (AssetType) second;
        AddrType addressVal = (AddrType) third;

        if (assetVal.getValue().getDecimals() != floatVal.getDecimals()) {
            trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
            return;
        }

        // Check that the value to withdraw is not negative
        if (floatVal.getInteger() < 0) {
            trap.raiseError(TrapErrorCodes.NEGATIVE_VALUE, executionPointer, Arrays.toString(instruction));
            return;
        }

        // Update the asset variable
        result = new AssetType(
                assetVal.getAssetId(),
                new FloatType(
                        assetVal.getValue().getInteger() - floatVal.getInteger(),
                        floatVal.getDecimals()
                )
        );

        globalSpace.put(variableName, new TraceChange(result, true));

        // Set up the single-use seal
        SingleUseSeal singleUseSeal = new SingleUseSeal(
                UUID.randomUUID().toString(),
                assetVal.getAssetId(),
                floatVal,
                addressVal.getAddress()
        );
        singleUseSealsToSend.add(singleUseSeal);
    }

    private void raiseOperation(String[] instruction) {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        String errorCode = instruction[1];

        switch (errorCode) {
            case "AMOUNT_NOT_EQUAL":
                trap.pushTrapError(
                        errorCode,
                        "The amount received in input is not equal to the amount required",
                        executionPointer,
                        Arrays.toString(instruction)
                );
                break;
            default:
                trap.raiseError(TrapErrorCodes.ERROR_CODE_DOES_NOT_EXISTS, executionPointer, Arrays.toString(instruction));
        }
    }
}

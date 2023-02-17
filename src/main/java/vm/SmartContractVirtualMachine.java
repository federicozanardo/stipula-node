package vm;

import exceptions.datastructures.stack.StackOverflowException;
import exceptions.datastructures.stack.StackUnderflowException;
import lib.datastructures.Stack;
import models.party.Party;
import models.contract.SingleUseSeal;
import models.dto.requests.event.CreateEventRequest;
import vm.trap.Trap;
import vm.trap.TrapErrorCodes;
import vm.types.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class SmartContractVirtualMachine {
    private final String[] instructions;
    private boolean isRunning = true;
    private int executionPointer = -1;
    private final int offset;
    private final Stack<Type> stack = new Stack<Type>(10);
    private final HashMap<String, Type> dataSpace = new HashMap<String, Type>(); // FIXME: rename
    private final HashMap<String, Type> argumentsSpace = new HashMap<String, Type>();
    private final HashMap<String, TraceChange> globalSpace;
    private final HashMap<String, SingleUseSeal> singleUseSealsToCreate = new HashMap<String, SingleUseSeal>();
    private final ArrayList<CreateEventRequest> createEventRequests = new ArrayList<CreateEventRequest>();
    private final Trap trap;
    // private String stuffToStore; // TODO: data to save in a blockchain transaction

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

    public HashMap<String, SingleUseSeal> getSingleUseSealsToCreate() {
        return singleUseSealsToCreate;
    }

    public ArrayList<CreateEventRequest> getCreateEventRequests() {
        return createEventRequests;
    }

    public boolean execute() {
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
                        case "TRIGGER":
                            this.triggerOperation(instruction);
                            break;
                        case "HALT":
                            this.haltOperation(instruction);
                            break;
                        default:
                            trap.raiseError(TrapErrorCodes.INSTRUCTION_DOES_NOT_EXISTS, executionPointer, Arrays.toString(instruction));
                    }
                }
            } catch (StackOverflowException exception) {
                trap.raiseError(TrapErrorCodes.STACK_OVERFLOW, executionPointer);
            } catch (StackUnderflowException exception) {
                trap.raiseError(TrapErrorCodes.STACK_UNDERFLOW, executionPointer);
            }
        }

        if (!trap.isStackEmpty()) {
            System.out.println("\nSmartContractVirtualMachine: execute => Errors in the stack");
            System.out.println(trap.printStack());
            return false;
        }

        System.out.println("SmartContractVirtualMachine: execute => Final state of the execution below");

        if (stack.isEmpty()) {
            System.out.println("SmartContractVirtualMachine: execute => The stack is empty");
        } else {
            System.out.println("SmartContractVirtualMachine: execute => Stack => " + stack.toString());
        }

        if (globalSpace.isEmpty()) {
            System.out.println("\nSmartContractVirtualMachine: execute => The global space is empty");
        } else {
            System.out.println("\nSmartContractVirtualMachine: execute => GlobalSpace");
            for (HashMap.Entry<String, TraceChange> entry : globalSpace.entrySet()) {
                TraceChange value = entry.getValue();
                if (value.getValue().getType().equals("party")) {
                    PartyType party = (PartyType) value.getValue();
                    System.out.println(entry.getKey() + ": " + party.getAddress() + " " + party.getPublicKey() + ", changed: " + value.isChanged());
                } else if (value.getValue().getType().equals("asset")) {
                    AssetType asset = (AssetType) value.getValue();
                    System.out.println(entry.getKey() + ": " + asset.getValue().getValue() + " " + asset.getAssetId() + ", changed: " + value.isChanged());
                } else {
                    System.out.println(entry.getKey() + ": " + value.getValue().getValue() + ", changed: " + value.isChanged());
                }
            }
        }

        if (argumentsSpace.isEmpty()) {
            System.out.println("\nSmartContractVirtualMachine: execute => The argument space is empty");
        } else {
            System.out.println("\nSmartContractVirtualMachine: execute => ArgumentsSpace");
            for (HashMap.Entry<String, Type> entry : argumentsSpace.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue().getValue());
            }
        }

        if (dataSpace.isEmpty()) {
            System.out.println("\nSmartContractVirtualMachine: execute => The data space is empty");
        } else {
            System.out.println("\nSmartContractVirtualMachine: execute => DataSpace");
            for (HashMap.Entry<String, Type> entry : dataSpace.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue().getValue());
            }
        }

        System.out.println("\nGlobal state of the execution" +
                "\nrunning -> " + isRunning +
                "\nexecutionPointer -> " + executionPointer +
                "\nexecutionPointer (with offset) -> " + (executionPointer + offset) +
                "\nlength of the program -> " + instructions.length +
                "\nlength of the program (with offset) -> " + (instructions.length + offset));

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

    // Instructions

    private void haltOperation(String[] instruction) {
        if (this.argumentsAreMoreThan(instruction, 0)) {
            return;
        }

        // Terminate the execution
        this.haltProgramExecution();
    }

    private void pushOperation(String[] instruction) throws StackOverflowException {
        if (this.argumentsAreLessThan(instruction, 2) || this.argumentsAreMoreThan(instruction, 4)) {
            return;
        }

        if ((instruction.length - 1) > 2 && !instruction[1].equals("real") && !instruction[1].equals("asset")) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (instruction.length - 1 > 3 && !instruction[1].equals("asset")) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        String type = instruction[1];
        String value = instruction[2];
        String decimals = "";
        String assetId = "";

        if (type.equals("real")) {
            decimals = instruction[3];
        }

        if (type.equals("asset")) {
            decimals = instruction[3];
            assetId = instruction[4];
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
            case "party":
                PartyType partyType;
                try {
                    partyType = new PartyType(new Party(value));
                    stack.push(partyType);
                } catch (NoSuchAlgorithmException exception) {
                    trap.raiseError(TrapErrorCodes.CRYPTOGRAPHIC_ALGORITHM_DOES_NOT_EXISTS, executionPointer, Arrays.toString(instruction));
                    break;
                }
                break;
            case "real":
                stack.push(new RealType(Integer.parseInt(value), Integer.parseInt(decimals)));
                break;
            case "asset":
                stack.push(new AssetType(assetId, new RealType(Integer.parseInt(value), Integer.parseInt(decimals))));
                break;
            case "time":
                if (value.equals("now")) {
                    long timestamp = System.currentTimeMillis() / 1000;
                    stack.push(new TimeType((int) timestamp));
                } else {
                    stack.push(new TimeType(Integer.parseInt(value)));
                }
                break;
            default:
                trap.raiseError(TrapErrorCodes.TYPE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
        }
    }

    private void addOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if (this.argumentsAreMoreThan(instruction, 0)) {
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();
        Type result;

        if (first.getType().equals("int") && second.getType().equals("int")) {
            IntType firstVal = (IntType) first;
            IntType secondVal = (IntType) second;
            result = new IntType(firstVal.getValue() + secondVal.getValue());
        } else if (first.getType().equals("real") && second.getType().equals("real")) {
            RealType firstVal = (RealType) first;
            RealType secondVal = (RealType) second;
            result = new RealType(firstVal.getInteger() + secondVal.getInteger(), firstVal.getDecimals());
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

            result = new RealType(
                    firstVal.getValue().getInteger() + secondVal.getValue().getInteger(),
                    firstVal.getValue().getDecimals()
            );
        } else if (first.getType().equals("asset") && second.getType().equals("real")) {
            AssetType firstVal = (AssetType) first;
            RealType secondVal = (RealType) second;

            if (firstVal.getValue().getDecimals() != secondVal.getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new RealType(
                    firstVal.getValue().getInteger() + secondVal.getInteger(),
                    firstVal.getValue().getDecimals()
            );
        } else if (first.getType().equals("real") && second.getType().equals("asset")) {
            RealType firstVal = (RealType) first;
            AssetType secondVal = (AssetType) second;

            if (firstVal.getDecimals() != secondVal.getValue().getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new RealType(
                    firstVal.getInteger() + secondVal.getValue().getInteger(),
                    firstVal.getDecimals()
            );
        } else if (first.getType().equals("time") && second.getType().equals("time")) {
            TimeType firstVal = (TimeType) first;
            TimeType secondVal = (TimeType) second;
            result = new TimeType(firstVal.getValue() + secondVal.getValue());
        } else {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }
        stack.push(result);
    }

    private void subOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if (this.argumentsAreMoreThan(instruction, 0)) {
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();
        Type result;

        if (first.getType().equals("int") && second.getType().equals("int")) {
            IntType firstVal = (IntType) first;
            IntType secondVal = (IntType) second;
            result = new IntType(firstVal.getValue() - secondVal.getValue());
        } else if (first.getType().equals("real") && second.getType().equals("real")) {
            RealType firstVal = (RealType) first;
            RealType secondVal = (RealType) second;
            result = new RealType(firstVal.getInteger() - secondVal.getInteger(), firstVal.getDecimals());
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

            result = new RealType(
                    firstVal.getValue().getInteger() - secondVal.getValue().getInteger(),
                    firstVal.getValue().getDecimals()
            );
        } else if (first.getType().equals("asset") && second.getType().equals("real")) {
            AssetType firstVal = (AssetType) first;
            RealType secondVal = (RealType) second;

            if (firstVal.getValue().getDecimals() != secondVal.getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new RealType(
                    firstVal.getValue().getInteger() - secondVal.getInteger(),
                    firstVal.getValue().getDecimals()
            );
        } else if (first.getType().equals("real") && second.getType().equals("asset")) {
            RealType firstVal = (RealType) first;
            AssetType secondVal = (AssetType) second;

            if (firstVal.getDecimals() != secondVal.getValue().getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new RealType(
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
        if (this.argumentsAreMoreThan(instruction, 0)) {
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();
        Type result;

        if (first.getType().equals("int") && second.getType().equals("int")) {
            IntType firstVal = (IntType) first;
            IntType secondVal = (IntType) second;
            result = new IntType(firstVal.getValue() * secondVal.getValue());
        } else if (first.getType().equals("real") && second.getType().equals("real")) {
            RealType firstVal = (RealType) first;
            RealType secondVal = (RealType) second;
            result = new RealType(
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

            result = new RealType(
                    (new Double(
                            (firstVal.getValue().getInteger() * secondVal.getValue().getInteger()) / Math.pow(10, firstVal.getValue().getDecimals()))
                    ).intValue(),
                    firstVal.getValue().getDecimals()
            );
        } else if (first.getType().equals("asset") && second.getType().equals("real")) {
            AssetType firstVal = (AssetType) first;
            RealType secondVal = (RealType) second;

            if (firstVal.getValue().getDecimals() != secondVal.getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new RealType(
                    (new Double(
                            (firstVal.getValue().getInteger() * secondVal.getInteger()) / Math.pow(10, secondVal.getDecimals()))
                    ).intValue(),
                    firstVal.getValue().getDecimals()
            );
        } else if (first.getType().equals("real") && second.getType().equals("asset")) {
            RealType firstVal = (RealType) first;
            AssetType secondVal = (AssetType) second;

            if (firstVal.getDecimals() != secondVal.getValue().getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new RealType(
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
        if (this.argumentsAreMoreThan(instruction, 0)) {
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

            result = new RealType(firstVal.getValue() / secondVal.getValue(), 2);
        } else if (first.getType().equals("real") && second.getType().equals("real")) {
            RealType firstVal = (RealType) first;
            RealType secondVal = (RealType) second;

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

            result = new RealType(
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

            result = new RealType(
                    (firstVal.getValue().getValue().divide(secondVal.getValue().getValue(), RoundingMode.CEILING))
                            .multiply(BigDecimal.valueOf(Math.pow(10, firstVal.getValue().getDecimals())))
                            .intValue(),
                    firstVal.getValue().getDecimals()
            );
        } else if (first.getType().equals("asset") && second.getType().equals("real")) {
            AssetType firstVal = (AssetType) first;
            RealType secondVal = (RealType) second;

            if (firstVal.getValue().getDecimals() != secondVal.getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            // Check if the denominator is zero
            if (secondVal.getInteger() == 0) {
                trap.raiseError(TrapErrorCodes.DIVISION_BY_ZERO, executionPointer, Arrays.toString(instruction));
                return;
            }

            result = new RealType(
                    (firstVal.getValue().getValue().divide(secondVal.getValue(), RoundingMode.CEILING))
                            .multiply(BigDecimal.valueOf(Math.pow(10, firstVal.getValue().getDecimals())))
                            .intValue(),
                    firstVal.getValue().getDecimals()
            );
        } else if (first.getType().equals("real") && second.getType().equals("asset")) {
            RealType firstVal = (RealType) first;
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

            result = new RealType(
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
        if (this.argumentsAreLessThan(instruction, 2) || this.argumentsAreMoreThan(instruction, 3)) {
            return;
        }

        if ((instruction.length - 1) > 2 && !instruction[1].equals("real")) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        String type = instruction[1];
        String variableName = instruction[2];
        String decimals = "";
        if (type.equals("real")) {
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
            case "party":
                dataSpace.put(variableName, new PartyType());
                break;
            case "real":
                dataSpace.put(variableName, new RealType(0, Integer.parseInt(decimals)));
                break;
            case "time":
                dataSpace.put(variableName, new TimeType());
                break;
            default:
                trap.raiseError(TrapErrorCodes.TYPE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
        }
    }

    private void loadOperation(String[] instruction) throws StackOverflowException {
        if (this.argumentsAreLessThan(instruction, 1) || this.argumentsAreMoreThan(instruction, 1)) {
            return;
        }

        String variableName = instruction[1];

        if (!dataSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }
        stack.push(dataSpace.get(variableName));
    }

    private void storeOperation(String[] instruction) throws StackUnderflowException {
        if (this.argumentsAreLessThan(instruction, 1) || this.argumentsAreMoreThan(instruction, 1)) {
            return;
        }

        String variableName = instruction[1];

        if (!dataSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }

        Type value = stack.pop();
        dataSpace.put(variableName, value);
    }

    private void andOperation(String[] instruction) throws StackUnderflowException, StackOverflowException {
        if (this.argumentsAreMoreThan(instruction, 0)) {
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
        if (this.argumentsAreMoreThan(instruction, 0)) {
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
        if (this.argumentsAreMoreThan(instruction, 0)) {
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
        if (this.argumentsAreLessThan(instruction, 1) || this.argumentsAreMoreThan(instruction, 1)) {
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
        if (this.argumentsAreLessThan(instruction, 1) || this.argumentsAreMoreThan(instruction, 1)) {
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
        if (this.argumentsAreMoreThan(instruction, 0)) {
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (first.getType().equals("asset") && second.getType().equals("real")) {
            AssetType firstAsset = (AssetType) first;
            RealType secondReal = (RealType) second;
            stack.push(new BoolType(Objects.equals(firstAsset.getValue().getValue(), secondReal.getValue())));
            return;
        } else if (first.getType().equals("real") && second.getType().equals("asset")) {
            RealType firstReal = (RealType) first;
            AssetType secondAsset = (AssetType) second;
            stack.push(new BoolType(Objects.equals(firstReal.getValue(), secondAsset.getValue().getValue())));
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
            case "party":
                PartyType firstAddr = new PartyType((Party) first.getValue());
                PartyType secondAddr = new PartyType((Party) second.getValue());
                stack.push(new BoolType(firstAddr.getValue().equals(secondAddr.getValue())));
                break;
            case "real":
                RealType firstReal = (RealType) first;
                RealType secondReal = (RealType) second;
                stack.push(new BoolType(Objects.equals(firstReal.getValue(), secondReal.getValue())));
                break;
            case "asset":
                AssetType firstAsset = (AssetType) first;
                AssetType secondAsset = (AssetType) second;

                boolean result = false;

                if (firstAsset.getAssetId().equals(secondAsset.getAssetId())) {
                    if (Objects.equals(firstAsset.getValue().getValue(), secondAsset.getValue().getValue())) {
                        result = true;
                    }
                }

                stack.push(new BoolType(result));
                break;
            case "time":
                TimeType firstTime = new TimeType((Integer) first.getValue());
                TimeType secondTime = new TimeType((Integer) second.getValue());
                stack.push(new BoolType(firstTime.getValue() == secondTime.getValue()));
                break;
            default:
                trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
        }
    }

    private void isltOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if (this.argumentsAreMoreThan(instruction, 0)) {
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals("int") && !first.getType().equals("real") && !first.getType().equals("asset")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (!second.getType().equals("int") && !second.getType().equals("real") && !second.getType().equals("asset")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (first.getType().equals("asset") && second.getType().equals("real")) {
            AssetType firstAsset = (AssetType) first;
            RealType secondReal = (RealType) second;
            stack.push(new BoolType(firstAsset.getValue().getValue().compareTo(secondReal.getValue()) < 0));
            return;
        } else if (first.getType().equals("real") && second.getType().equals("asset")) {
            RealType firstReal = (RealType) first;
            AssetType secondAsset = (AssetType) second;
            stack.push(new BoolType(firstReal.getValue().compareTo(secondAsset.getValue().getValue()) < 0));
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
            case "real":
                RealType firstReal = (RealType) first;
                RealType secondReal = (RealType) second;
                stack.push(new BoolType(firstReal.getValue().compareTo(secondReal.getValue()) < 0));
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
        if (this.argumentsAreMoreThan(instruction, 0)) {
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals("int") && !first.getType().equals("real") && !first.getType().equals("asset")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (!second.getType().equals("int") && !second.getType().equals("real") && !second.getType().equals("asset")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (first.getType().equals("asset") && second.getType().equals("real")) {
            AssetType firstAsset = (AssetType) first;
            RealType secondReal = (RealType) second;
            stack.push(new BoolType(firstAsset.getValue().getValue().compareTo(secondReal.getValue()) <= 0));
            return;
        } else if (first.getType().equals("real") && second.getType().equals("asset")) {
            RealType firstReal = (RealType) first;
            AssetType secondAsset = (AssetType) second;
            stack.push(new BoolType(firstReal.getValue().compareTo(secondAsset.getValue().getValue()) <= 0));
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
                stack.push(new BoolType(firstInt.getValue() <= secondInt.getValue()));
                break;
            case "real":
                RealType firstReal = (RealType) first;
                RealType secondReal = (RealType) second;
                stack.push(new BoolType(firstReal.getValue().compareTo(secondReal.getValue()) <= 0));
                break;
            default:
                trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
        }
    }

    private void ainstOperation(String[] instruction) {
        if (this.argumentsAreLessThan(instruction, 2)) {
            return;
        }

        if ((instruction.length - 1) > 2 && !instruction[1].equals("real") && !instruction[1].equals("asset")) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (instruction.length - 1 > 3 && !instruction[1].equals("asset")) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (this.argumentsAreMoreThan(instruction, 4)) {
            return;
        }

        String type = instruction[1];
        String variableName = instruction[2];
        String decimals = "";
        String assetId = "";

        if (type.equals("real")) {
            decimals = instruction[3];
        }

        if (type.equals("asset")) {
            decimals = instruction[3];
            assetId = instruction[4];
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
            case "party":
                argumentsSpace.put(variableName, new PartyType());
                break;
            case "real":
                argumentsSpace.put(variableName, new RealType(0, Integer.parseInt(decimals)));
                break;
            case "asset":
                AssetType value = new AssetType(assetId, new RealType(0, Integer.parseInt(decimals)));
                argumentsSpace.put(variableName, value);
                break;
            case "time":
                argumentsSpace.put(variableName, new TimeType());
                break;
            default:
                trap.raiseError(TrapErrorCodes.TYPE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
        }
    }

    private void aloadOperation(String[] instruction) throws StackOverflowException {
        if (this.argumentsAreLessThan(instruction, 1) || this.argumentsAreMoreThan(instruction, 1)) {
            return;
        }

        String variableName = instruction[1];

        if (!argumentsSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }

        stack.push(argumentsSpace.get(variableName));
    }

    private void astoreOperation(String[] instruction) throws StackUnderflowException {
        if (this.argumentsAreLessThan(instruction, 1) || this.argumentsAreMoreThan(instruction, 1)) {
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

            // Check that the element popped from the stack is a real value
            if (!value.getType().equals("asset")) {
                trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
                return;
            }

            AssetType assetValuePopped = (AssetType) value;

            // Check that the element popped from the stack has the same decimals of the asset
            if (assetValuePopped.getValue().getDecimals() != currentAssetValue.getValue().getDecimals()) {
                trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            // Check that the element popped from the stack has the same asset id
            if (!assetValuePopped.getAssetId().equals(currentAssetValue.getAssetId())) {
                trap.raiseError(TrapErrorCodes.ASSET_IDS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
                return;
            }

            AssetType newValue = new AssetType(
                    assetValuePopped.getAssetId(),
                    new RealType(
                            assetValuePopped.getValue().getInteger(),
                            assetValuePopped.getValue().getDecimals()
                    )
            );
            argumentsSpace.put(variableName, newValue);
        } else {
            Type value = stack.pop();
            argumentsSpace.put(variableName, value);
        }
    }

    private void ginstOperation(String[] instruction) {
        if (this.argumentsAreLessThan(instruction, 2) || this.argumentsAreMoreThan(instruction, 4)) {
            return;
        }

        if ((instruction.length - 1) > 2 && !instruction[1].equals("real") && !instruction[1].equals("asset")) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        if (instruction.length - 1 > 3 && !instruction[1].equals("asset")) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, executionPointer, Arrays.toString(instruction));
            return;
        }

        String type = instruction[1];
        String variableName = instruction[2];
        String decimals = "";
        String assetId = "";

        if (type.equals("real")) {
            decimals = instruction[3];
        }

        if (type.equals("asset")) {
            decimals = instruction[3];
            assetId = instruction[4];
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
            case "party":
                globalSpace.put(variableName, new TraceChange(new PartyType(), true));
                break;
            case "real":
                globalSpace.put(variableName, new TraceChange(new RealType(0, Integer.parseInt(decimals)), true));
                break;
            case "asset":
                AssetType value = new AssetType(assetId, new RealType(0, Integer.parseInt(decimals)));
                globalSpace.put(variableName, new TraceChange(value, true));
                break;
            case "time":
                globalSpace.put(variableName, new TraceChange(new TimeType(), true));
                break;
            default:
                trap.raiseError(TrapErrorCodes.TYPE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
        }
    }

    private void gloadOperation(String[] instruction) throws StackOverflowException {
        if (this.argumentsAreLessThan(instruction, 1) || this.argumentsAreMoreThan(instruction, 1)) {
            return;
        }

        String variableName = instruction[1];

        if (!globalSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }

        stack.push(globalSpace.get(variableName).getValue());
    }

    private void gstoreOperation(String[] instruction) throws StackUnderflowException {
        if (this.argumentsAreLessThan(instruction, 1) || this.argumentsAreMoreThan(instruction, 1)) {
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

        globalSpace.put(variableName, new TraceChange(value, true));
    }

    private void depositOperation(String[] instruction) throws StackUnderflowException {
        if (this.argumentsAreLessThan(instruction, 1) || this.argumentsAreMoreThan(instruction, 1)) {
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
                new RealType(
                        assetContract.getValue().getInteger() + assetToDeposit.getValue().getInteger(),
                        assetToDeposit.getValue().getDecimals()
                )
        );

        globalSpace.put(variableName, new TraceChange(result, true));
    }

    private void withdrawOperation(String[] instruction) throws StackUnderflowException {
        if (this.argumentsAreLessThan(instruction, 1) || this.argumentsAreMoreThan(instruction, 1)) {
            return;
        }

        String variableName = instruction[1];

        if (!globalSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, executionPointer, Arrays.toString(instruction));
            return;
        }

        Type third = stack.pop();   // Party
        Type second = stack.pop();  // Asset
        Type first = stack.pop();   // Real
        AssetType result;

        if (!first.getType().equals("real") || !second.getType().equals("asset") || !third.getType().equals("party")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        RealType realVal = (RealType) first;
        AssetType assetVal = (AssetType) second;
        PartyType partyVal = (PartyType) third;

        if (assetVal.getValue().getDecimals() != realVal.getDecimals()) {
            trap.raiseError(TrapErrorCodes.DECIMALS_DOES_NOT_MATCH, executionPointer, Arrays.toString(instruction));
            return;
        }

        // Check that the value to withdraw is not negative
        if (realVal.getInteger() < 0) {
            trap.raiseError(TrapErrorCodes.NEGATIVE_VALUE, executionPointer, Arrays.toString(instruction));
            return;
        }

        // Update the asset variable
        result = new AssetType(
                assetVal.getAssetId(),
                new RealType(
                        assetVal.getValue().getInteger() - realVal.getInteger(),
                        realVal.getDecimals()
                )
        );

        globalSpace.put(variableName, new TraceChange(result, true));

        // Set up the single-use seal
        SingleUseSeal singleUseSeal = new SingleUseSeal(
                assetVal.getAssetId(),
                realVal,
                partyVal.getAddress()
        );
        singleUseSealsToCreate.put(partyVal.getAddress(), singleUseSeal);
    }

    private void raiseOperation(String[] instruction) {
        if (this.argumentsAreLessThan(instruction, 1) || this.argumentsAreMoreThan(instruction, 1)) {
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

    private void triggerOperation(String[] instruction) throws StackUnderflowException {
        if (this.argumentsAreLessThan(instruction, 1) || this.argumentsAreMoreThan(instruction, 1)) {
            return;
        }

        String obligationFunctionName = instruction[1];

        Type value = stack.pop();   // Time

        if (!value.getType().equals("time")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, executionPointer, Arrays.toString(instruction));
            return;
        }

        TimeType timeVal = (TimeType) value;

        // Check that the value is not negative
        if (timeVal.getValue() <= 0) {
            trap.raiseError(TrapErrorCodes.LESS_THAN_ZERO, executionPointer, Arrays.toString(instruction));
            return;
        }

        CreateEventRequest createEventRequest = new CreateEventRequest(obligationFunctionName, timeVal.getValue());
        createEventRequests.add(createEventRequest);
    }
}

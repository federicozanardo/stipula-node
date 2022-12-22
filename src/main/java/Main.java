import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

import datastructures.Stack;
import exceptions.stack.StackOverflowException;
import exceptions.stack.StackUnderflowException;
import exceptions.trap.TrapException;
import instructions.Instruction;
import instructions.math.Add;
import instructions.math.Div;
import instructions.math.Mul;
import instructions.math.Sub;
import trap.Trap;
import trap.TrapErrorCodes;
import types.BoolType;
import types.IntType;
import types.StringType;
import types.Type;

class Main {
    private static boolean running = true;
    private static int i = -1;
    private static final Stack<Type> stack = new Stack<Type>(10);
    private static final HashMap<String, Type> dataSpace = new HashMap<String, Type>();
    private static final HashMap<String, Type> argumentsSpace = new HashMap<String, Type>();
    private static final HashMap<String, Type> globalSpace = new HashMap<String, Type>();
    private static final Trap trap = new Trap();
    // private static String stuffToStore; // TODO: data to save in a blockchain transaction
    // private static int programCounter; // or instructionPointer
    // private static int stackPointer;

    public static void main(String[] args) {
        File currentDirectory = new File(new File(".").getAbsolutePath());
        String path = currentDirectory + "/examples/";

        System.out.println("Loading the program...");
        String bytecode = readProgram(path + "program7.sb");
        System.out.println("Program loaded");

        System.out.println("Program\n" + bytecode);

        String[] instructions = bytecode.split("\n");

//        if (!instructions[instructions.length - 1].equals("HALT")) {
//            trap.raiseError(TrapErrorCodes.MISS_HALT_INSTRUCTION, instructions.length);
//        }

        while (running) {
            if (!trap.isStackEmpty()) {
                haltProgramExecution();
                break;
            }

            try {
                i++;
                String singleInstruction = instructions[i].trim();
                String[] instruction = singleInstruction.split(" ");

                if (!(instruction.length == 1 && instruction[0].substring(instruction[0].length() - 1).equals(":"))) {
                    switch (instruction[0]) {
                        case "PUSH":
                            pushOperation(instruction);
                            break;
                        case "ADD":
                            addOperation(instruction);
                            break;
                        case "SUB":
                            subOperation(instruction);
                            break;
                        case "MUL":
                            mulOperation(instruction);
                            break;
                        case "DIV":
                            divOperation(instruction);
                            break;
                        case "INST":
                            instOperation(instruction);
                            break;
                        case "LOAD":
                            loadOperation(instruction);
                            break;
                        case "STORE":
                            storeOperation(instruction);
                            break;
                        case "JMP":
                            jmpOperation(instruction, instructions);
                            break;
                        case "JMPIF":
                            jmpifOperation(instruction, instructions);
                            break;
                        case "ISEQ":
                            iseqOperation(instruction);
                            break;
                        case "ISGE":
                            isgeOperation(instruction);
                            break;
                        case "ISGT":
                            isgtOperation(instruction);
                            break;
                        case "ISLE":
                            isleOperation(instruction);
                            break;
                        case "ISLT":
                            isltOperation(instruction);
                            break;
                        case "HALT":
                            if ((instruction.length - 1) > 0) {
                                trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
                                break;
                            }

                            // Terminate the program
                            haltProgramExecution();
                            break;
                        case "AINST":
                            ainstOperation(instruction);
                            break;
                        case "ASTORE":
                            astoreOperation(instruction);
                            break;
                        case "ALOAD":
                            aloadOperation(instruction);
                            break;
                        case "GINST":
                            ginstOperation(instruction);
                            break;
                        case "GSTORE":
                            gstoreOperation(instruction);
                            break;
                        case "GLOAD":
                            gloadOperation(instruction);
                            break;
                        default:
                            trap.raiseError(TrapErrorCodes.INSTRUCTION_DOES_NOT_EXISTS, (i + 1), instruction[0]);
                    }
                }
            } catch (StackOverflowException | StackUnderflowException error) {
                trap.raiseError(error.getCode(), (i + 1));
            }
        }

        System.out.println("\nFinal state of the execution below");

        if (stack.isEmpty()) {
            System.out.println("\nThe stack is empty");
        } else {
            System.out.println("\nStack");
            while (!stack.isEmpty()) {
                try {
                    System.out.println("Value: " + stack.pop().getValue().toString());
                } catch (StackUnderflowException error) {
                    System.exit(-1);
                }
            }
        }

        System.out.println("\nGlobalSpace");
        for (HashMap.Entry<String, Type> entry : globalSpace.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue().getValue());
        }

        System.out.println("\nArgumentsSpace");
        for (HashMap.Entry<String, Type> entry : argumentsSpace.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue().getValue());
        }

        System.out.println("\nDataSpace");
        for (HashMap.Entry<String, Type> entry : dataSpace.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue().getValue());
        }

        System.out.println("\nGlobal state of the execution" +
                "\nrunning -> " + Boolean.toString(running) +
                "\ni -> " + Integer.toString(i) +
                "\nlength of the program -> " + Integer.toString(instructions.length));

        if (!trap.isStackEmpty()) {
            System.out.println("\nErrors in the stack");
            System.out.println(trap.printStack());
        }
    }

    private static void haltProgramExecution() {
        running = false;
    }

    private static String readProgram(String pathname) {
        String bytecode = new String("");

        boolean startMultilineComment = false;
        boolean endMultilineComment = false;
        boolean isCommentLine = false;

        try {
            File file = new File(pathname);
            Scanner fileReader = new Scanner(file);

            while (fileReader.hasNextLine()) {
                String data = fileReader.nextLine().trim();

                if (data.substring(0, 2).equals("/*")) {
                    startMultilineComment = true;
                }

                if (data.substring(0, 2).equals("*/")) {
                    endMultilineComment = true;
                }

                if (data.substring(0, 2).equals("//")) {
                    isCommentLine = true;
                    System.out.println("comment line: " + data);
                }

                // It is an non-comment line, so add it to the bytecode
                if (!startMultilineComment && !isCommentLine) {
                    bytecode += data + "\n";
                }

                if (endMultilineComment) {
                    startMultilineComment = false;
                }

                if (isCommentLine) {
                    isCommentLine = false;
                }
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("readProgram: An error occurred while reading the file.");
            e.printStackTrace();
        }

        return bytecode;
    }

    // Instructions

    private static void pushOperation(String[] instruction) throws StackOverflowException {
        if ((instruction.length - 1) < 2) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 2) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        String type = instruction[1];
        String value = instruction[2];

        switch (type) {
            case "int":
                stack.push(new IntType(Integer.parseInt(value)));
                break;
            case "bool":
                stack.push(new BoolType(Boolean.parseBoolean(value)));
                break;
            case "str":
                stack.push(new StringType(value));
                break;
            default:
                trap.raiseError(TrapErrorCodes.TYPE_DOES_NOT_EXIST, (i + 1));
        }
    }

    private static void addOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();
        Instruction addInstruction = new Add(first, second);
        try {
            IntType result = (IntType) addInstruction.execute();
            stack.push(result);
        } catch (TrapException error) {
            trap.raiseError(error.getCode(), (i + 1), addInstruction.getName());
        }
    }

    private static void subOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();
        Instruction subInstruction = new Sub(first, second);
        try {
            IntType result = (IntType) subInstruction.execute();
            stack.push(result);
        } catch (TrapException error) {
            trap.raiseError(error.getCode(), (i + 1), subInstruction.getName());
        }
    }

    private static void mulOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();
        Instruction mulInstruction = new Mul(first, second);
        try {
            IntType result = (IntType) mulInstruction.execute();
            stack.push(result);
        } catch (TrapException error) {
            trap.raiseError(error.getCode(), (i + 1), mulInstruction.getName());
        }
    }

    private static void divOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();
        Instruction divInstruction = new Div(first, second);
        try {
            IntType result = (IntType) divInstruction.execute();
            stack.push(result);
        } catch (TrapException error) {
            trap.raiseError(error.getCode(), (i + 1), divInstruction.getName());
        }
    }

    private static void instOperation(String[] instruction) {
        if ((instruction.length - 1) < 2) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 2) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        String type = instruction[1];
        String variableName = instruction[2];

        if (dataSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_ALREADY_EXIST, (i + 1));
        }

        switch (type) {
            case "int":
                dataSpace.put(variableName, new IntType());
                break;
            case "str":
                dataSpace.put(variableName, new StringType());
                break;
            default:
                trap.raiseError(TrapErrorCodes.TYPE_DOES_NOT_EXIST, (i + 1));
        }
    }

    private static void loadOperation(String[] instruction) throws StackOverflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        String variableName = instruction[1];

        if (!dataSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, (i + 1));
        }
        stack.push(dataSpace.get(variableName));
    }

    private static void storeOperation(String[] instruction) throws StackUnderflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        String variableName = instruction[1];

        if (!dataSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_ALREADY_EXIST, (i + 1));
        }
        Type value = stack.pop();
        dataSpace.put(variableName, value);
    }

    private static void jmpOperation(String[] instruction, String[] singleInstructions) {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        String label = instruction[1];

        int j = i;
        boolean found = false;

        while (!found && j < singleInstructions.length) {
            if (singleInstructions[j].trim().equals(label + ":")) {
                found = true;
            }
            j++;
        }

        if (found) {
            i = j - 1;
        } else {
            trap.raiseError(TrapErrorCodes.LABEL_DOES_NOT_EXISTS, (i + 1));
        }
    }

    private static void jmpifOperation(String[] instruction, String[] singleInstructions) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        Type resultOfEvaluation = stack.pop();

        if (!resultOfEvaluation.getType().equals("bool")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1));
            return;
        }

        BoolType resultOfEvaluationVal = new BoolType((Boolean) resultOfEvaluation.getValue());

        if (resultOfEvaluationVal.getValue()) {
            jmpOperation(instruction, singleInstructions);
        } else {
            stack.push(resultOfEvaluation);
        }
    }

    private static void iseqOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals(second.getType())) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1));
            return;
        }

        if (!first.getType().equals("int") && !first.getType().equals("str") && !first.getType().equals("bool") && !first.getType().equals("addr")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1));
            return;
        }

        if (first.getType().equals("int")) {
            IntType firstVal = new IntType((Integer) first.getValue());
            IntType secondVal = new IntType((Integer) second.getValue());
            stack.push(new BoolType(firstVal.getValue() == secondVal.getValue()));
            return;
        }

        if (first.getType().equals("bool")) {
            BoolType firstVal = new BoolType((Boolean) first.getValue());
            BoolType secondVal = new BoolType((Boolean) second.getValue());
            stack.push(new BoolType(firstVal.getValue() == secondVal.getValue()));
            return;
        }

        if (first.getType().equals("str")) {
            StringType firstVal = new StringType((String) first.getValue());
            StringType secondVal = new StringType((String) second.getValue());
            stack.push(new BoolType(firstVal.getValue().equals(secondVal.getValue())));
            return;
        }

        if (first.getType().equals("addr")) {
//            IntType firstVal = new IntType((Integer) first.getValue());
//            IntType secondVal = new IntType((Integer) second.getValue());
//            stack.push(new BoolType(firstVal.getValue() == secondVal.getValue()));
//            return;
        }
    }

    private static void isgtOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals("int") || !second.getType().equals("int")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1));
            return;
        }

        IntType firstVal = new IntType((Integer) first.getValue());
        IntType secondVal = new IntType((Integer) second.getValue());
        stack.push(new BoolType(firstVal.getValue() > secondVal.getValue()));
    }

    private static void isgeOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals("int") || !second.getType().equals("int")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1));
            return;
        }

        IntType firstVal = new IntType((Integer) first.getValue());
        IntType secondVal = new IntType((Integer) second.getValue());
        stack.push(new BoolType(firstVal.getValue() >= secondVal.getValue()));
    }

    private static void isltOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals("int") || !second.getType().equals("int")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1));
            return;
        }

        IntType firstVal = new IntType((Integer) first.getValue());
        IntType secondVal = new IntType((Integer) second.getValue());
        stack.push(new BoolType(firstVal.getValue() < secondVal.getValue()));
    }

    private static void isleOperation(String[] instruction) throws StackOverflowException, StackUnderflowException {
        if ((instruction.length - 1) > 0) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals("int") || !second.getType().equals("int")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1));
            return;
        }

        IntType firstVal = new IntType((Integer) first.getValue());
        IntType secondVal = new IntType((Integer) second.getValue());
        stack.push(new BoolType(firstVal.getValue() <= secondVal.getValue()));
    }

    private static void ainstOperation(String[] instruction) {
        if ((instruction.length - 1) < 2) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 2) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        String type = instruction[1];
        String variableName = instruction[2];

        if (argumentsSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_ALREADY_EXIST, (i + 1));
        }

        switch (type) {
            case "int":
                argumentsSpace.put(variableName, new IntType());
                break;
            case "str":
                argumentsSpace.put(variableName, new StringType());
                break;
            default:
                trap.raiseError(TrapErrorCodes.TYPE_DOES_NOT_EXIST, (i + 1));
        }
    }

    private static void astoreOperation(String[] instruction) throws StackUnderflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        String variableName = instruction[1];

        if (!argumentsSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_ALREADY_EXIST, (i + 1));
        }
        Type value = stack.pop();
        argumentsSpace.put(variableName, value);
    }

    private static void aloadOperation(String[] instruction) throws StackOverflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        String variableName = instruction[1];

        if (!argumentsSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, (i + 1));
        }
        stack.push(argumentsSpace.get(variableName));
    }

    private static void ginstOperation(String[] instruction) {
        if ((instruction.length - 1) < 2) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 2) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        String type = instruction[1];
        String variableName = instruction[2];

        if (globalSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_ALREADY_EXIST, (i + 1));
        }

        switch (type) {
            case "int":
                globalSpace.put(variableName, new IntType());
                break;
            case "str":
                globalSpace.put(variableName, new StringType());
                break;
            default:
                trap.raiseError(TrapErrorCodes.TYPE_DOES_NOT_EXIST, (i + 1));
        }
    }

    private static void gstoreOperation(String[] instruction) throws StackUnderflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        String variableName = instruction[1];

        if (!globalSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_ALREADY_EXIST, (i + 1));
        }
        Type value = stack.pop();
        globalSpace.put(variableName, value);
    }

    private static void gloadOperation(String[] instruction) throws StackOverflowException {
        if ((instruction.length - 1) < 1) {
            trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        if ((instruction.length - 1) > 1) {
            trap.raiseError(TrapErrorCodes.TOO_MANY_ARGUMENTS, (i + 1), instruction[0]);
            return;
        }

        String variableName = instruction[1];

        if (!globalSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, (i + 1));
        }
        stack.push(globalSpace.get(variableName));
    }
}
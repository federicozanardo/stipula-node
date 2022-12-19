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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

class Main {
    private static boolean running = true;
    private static int i = -1;
    private static Stack<Type> stack = new Stack<Type>(10);
    private static HashMap<String, Type> dataSpace = new HashMap<String, Type>();
    private static Trap trap = new Trap();
    // private static String stuffToStore; // TODO: data to save in a blockchain transaction
    // private static int programCounter; // or instructionPointer
    // private static int stackPointer;

    public static void main(String[] args) {
        File currentDirectory = new File(new File(".").getAbsolutePath());
        String path = currentDirectory + "/examples/";

        System.out.println("Loading the program...");
        String bytecode = readProgram(path + "program1.sb");
        System.out.println("Program loaded");

        System.out.println("Program\n" + bytecode);

        String[] singleInstructions = bytecode.split("\n");

        if (!singleInstructions[singleInstructions.length - 1].equals("HALT")) {
            trap.raiseError(TrapErrorCodes.MISS_HALT_INSTRUCTION, singleInstructions.length);
        }

        while (running) {
            if (!trap.isStackEmpty()) {
                haltProgramExecution();
            } else {
                try {
                    i++;
                    String singleInstruction = singleInstructions[i].trim();
                    String[] instruction = singleInstruction.split(" ");

                    if (!(instruction.length == 1 && instruction[0].substring(instruction[0].length() - 1).equals(":"))) {
                        switch (instruction[0]) {
                            case "PUSH":
                                if ((instruction.length - 1) < 2) {
                                    trap.raiseError(TrapErrorCodes.NOT_ENOUGH_ARGUMENTS, (i + 1), instruction[0]);
                                    break;
                                }

                                System.out.println("HAVE YOU BEEN HERE?");
                                pushOperation(instruction[1], instruction[2]);
                                break;
                            case "ADD":
                                addOperation();
                                break;
                            case "SUB":
                                subOperation();
                                break;
                            case "MUL":
                                mulOperation();
                                break;
                            case "DIV":
                                divOperation();
                                break;
                            case "INST":
                                if ((instruction.length - 1) < 2) {
                                    // TRAP
                                }
                                instOperation(instruction[1], instruction[2]);
                                break;
                            case "LOAD":
                                if ((instruction.length - 1) < 1) {
                                    // TRAP
                                }
                                loadOperation(instruction[1]);
                                break;
                            case "STORE":
                                if ((instruction.length - 1) < 1) {
                                    // TRAP
                                }
                                storeOperation(instruction[1]);
                                break;
                            case "JMP":
                                if ((instruction.length - 1) < 1) {
                                    // TRAP
                                }
                                jmpOperation(instruction[1], singleInstructions);
                                break;
                            case "JMPIF":
                                if ((instruction.length - 1) < 1) {
                                    // TRAP
                                }

                                Type resultOfEvaluation = stack.pop();

                                if (!resultOfEvaluation.getType().equals("bool")) {
                                    trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1));
                                } else {
                                    BoolType resultOfEvaluationVal = new BoolType((Boolean) resultOfEvaluation.getValue());

                                    if (resultOfEvaluationVal.getValue()) {
                                        jmpOperation(instruction[1], singleInstructions);
                                    } else {
                                        stack.push(resultOfEvaluation);
                                    }
                                }
                                break;
                            case "ISEQ":
                                iseqOperation();
                                break;
                            case "ISGE":
                                isgeOperation();
                                break;
                            case "ISGT":
                                isgtOperation();
                                break;
                            case "ISLE":
                                isleOperation();
                                break;
                            case "ISLT":
                                isltOperation();
                                break;
                            case "HALT":
                                // Terminate the program
                                haltProgramExecution();
                                break;
                            default:
                                trap.raiseError(TrapErrorCodes.INSTRUCTION_DOES_NOT_EXISTS, (i + 1), instruction[0]);
                        }
                    }
                } catch (StackOverflowException | StackUnderflowException error) {
                    trap.raiseError(error.getCode(), (i + 1));
                }
            }
        }

        System.out.println("\nFinal state of the execution below");

        if (stack.isEmpty()) {
            System.out.println("\nThe stack is empty");
        } else {
            System.out.println("\nStack");
            while (stack.isEmpty() == false) {
                try {
                    System.out.println("Value: " + stack.pop().getValue().toString());
                } catch (StackUnderflowException error) {
                    System.exit(-1);
                }
            }
        }

        System.out.println("\nDataSpace");
        for (HashMap.Entry<String, Type> entry : dataSpace.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue().getValue());
        }

        System.out.println("\nGlobal state of the execution" +
                "\nrunning -> " + Boolean.toString(running) +
                "\ni -> " + Integer.toString(i) +
                "\nlength of the program -> " + Integer.toString(singleInstructions.length));

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

    private static void pushOperation(String type, String value) throws StackOverflowException {
        switch (type) {
            case "int":
                stack.push(new IntType(Integer.parseInt(value)));
                break;
            case "str":
                stack.push(new StringType(value));
                break;
            default:
                trap.raiseError(TrapErrorCodes.TYPE_DOES_NOT_EXIST, (i + 1));
        }
    }

    private static void addOperation() throws StackOverflowException, StackUnderflowException {
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

    private static void subOperation() throws StackOverflowException, StackUnderflowException {
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

    private static void mulOperation() throws StackOverflowException, StackUnderflowException {
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

    private static void divOperation() throws StackOverflowException, StackUnderflowException {
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

    private static void instOperation(String type, String variableName) {
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

    private static void loadOperation(String variableName) throws StackOverflowException {
        if (!dataSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_DOES_NOT_EXIST, (i + 1));
        }
        stack.push(dataSpace.get(variableName));
    }

    private static void storeOperation(String variableName) throws StackUnderflowException {
        if (!dataSpace.containsKey(variableName)) {
            trap.raiseError(TrapErrorCodes.VARIABLE_ALREADY_EXIST, (i + 1));
        }
        Type value = stack.pop();
        dataSpace.put(variableName, value);
    }

    private static void jmpOperation(String label, String[] singleInstructions) {
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

    private static void iseqOperation() throws StackOverflowException, StackUnderflowException {
        Type second = stack.pop();
        Type first = stack.pop();

        if (!first.getType().equals("int") || !second.getType().equals("int")) {
            trap.raiseError(TrapErrorCodes.INCORRECT_TYPE, (i + 1));
            return;
        }

        IntType firstVal = new IntType((Integer) first.getValue());
        IntType secondVal = new IntType((Integer) second.getValue());
        stack.push(new BoolType(firstVal.getValue() == secondVal.getValue()));
    }

    private static void isgtOperation() throws StackOverflowException, StackUnderflowException {
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

    private static void isgeOperation() throws StackOverflowException, StackUnderflowException {
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

    private static void isltOperation() throws StackOverflowException, StackUnderflowException {
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

    private static void isleOperation() throws StackOverflowException, StackUnderflowException {
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
}
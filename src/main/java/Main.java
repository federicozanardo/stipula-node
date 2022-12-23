import java.io.File;
import java.io.FileNotFoundException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import datastructures.Stack;
import exceptions.stack.StackOverflowException;
import exceptions.stack.StackUnderflowException;
import exceptions.trap.TrapException;
import instructions.Instruction;
import instructions.math.Add;
import instructions.math.Div;
import instructions.math.Mul;
import instructions.math.Sub;
import messages.AgreementCallMessage;
import messages.Message;
import messages.SignedMessage;
import trap.Trap;
import trap.TrapErrorCodes;
import types.BoolType;
import types.IntType;
import types.StrType;
import types.Type;
import types.address.Address;

import static crypto.Crypto.generateKeyPair;
import static crypto.Crypto.sign;

class Main {
    private static int offset = 0;

    public static void main(String[] args) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        File currentDirectory = new File(new File(".").getAbsolutePath());
        String path = currentDirectory + "/examples/";

        Base64.Encoder encoder = Base64.getEncoder();

        KeyPair lenderKeys = generateKeyPair();
        KeyPair borrowerKeys = generateKeyPair();

        Address lenderPubKey = new Address(encoder.encodeToString(lenderKeys.getPublic().getEncoded()));
        Address borrowerPubKey = new Address(encoder.encodeToString(borrowerKeys.getPublic().getEncoded()));
        HashMap<String, Address> parties = new HashMap<>();
        parties.put("Lender", lenderPubKey);
        parties.put("Borrower", borrowerPubKey);

        HashMap<String, String> argumentsMessage = new HashMap<>();
        argumentsMessage.put("cost", "12");
        argumentsMessage.put("rent_time", "1");

        AgreementCallMessage agreementCallMessage = new AgreementCallMessage("asd123", argumentsMessage, parties);

        String lenderSign = sign(agreementCallMessage.toString(), lenderKeys.getPrivate());
        String borrowerSign = sign(agreementCallMessage.toString(), borrowerKeys.getPrivate());
        HashMap<String, String> signatures = new HashMap<>();
        signatures.put("Lender", lenderSign);
        signatures.put("Borrower", borrowerSign);

        SignedMessage signedMessage = new SignedMessage(agreementCallMessage, signatures);

        // Load the function
        // String bytecode = readProgram(path + "program7.sb");
        String rawBytecode = loadFunction(path + "contract1.sb");

        // Load arguments
        HashMap<String, String> arguments = loadArguments(agreementCallMessage);

        // Load the bytecode
        String bytecode = loadBytecode(rawBytecode, arguments);
        String[] instructions = bytecode.split("\n");

        // Execute the code
        VirtualMachine vm = new VirtualMachine(instructions, offset);
        vm.execute();
    }

    private static String readProgram(String pathname) {
        String bytecode = "";

        boolean startMultilineComment = false;
        boolean endMultilineComment = false;
        boolean isCommentLine = false;

        System.out.println("readProgram: Loading the program...");
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
                    // System.out.println("comment line: " + data);
                }

                // It is a non-comment line, so add it to the bytecode
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
        System.out.println("readProgram: Program loaded");
        // System.out.println("readProgram: Program\n" + bytecode);

        return bytecode;
    }

    private static String loadFunction(String pathname) {
        String bytecodeFunction = "";
        boolean isFunctionStarted = false;
        boolean isFunctionEnded = false;

        // Load all the bytecode
        String bytecode = readProgram(pathname);
        String[] instructions = bytecode.split("\n");

        System.out.println("loadFunction: Loading the function...");
        for (int i = 0; i < instructions.length && !isFunctionEnded; i++) {
            String[] instruction = instructions[i].trim().split(" ");

            if (isFunctionStarted) {
                bytecodeFunction += instructions[i].trim() + "\n";
            }

            if (instruction[0].equals("fn") && instruction[1].equals("agreement")) {
                isFunctionStarted = true;
                offset = i + 1;
                // bytecodeFunction += instructions[i].trim() + "\n";
            }

            if (instruction[0].equals("HALT")) {
                isFunctionEnded = true;
            }
        }
        System.out.println("loadFunction: Function loaded");
        System.out.println("loadFunction: Function\n" + bytecodeFunction);

        return bytecodeFunction;
    }

    private static HashMap<String, String> loadArguments(Message message) {
        HashMap<String, String> arguments = new HashMap<>();

        if (message instanceof AgreementCallMessage) {
            for (HashMap.Entry<String, String> entry : ((AgreementCallMessage) message).arguments.entrySet()) {
                arguments.put(entry.getKey(), entry.getValue());
            }
            for (HashMap.Entry<String, Address> entry : ((AgreementCallMessage) message).parties.entrySet()) {
                arguments.put(entry.getKey(), entry.getValue().getPublicKey());
            }
            return arguments;
        } else {
            return null;
        }
    }

    private static String loadBytecode(String rawBytecode, HashMap<String, String> arguments) {
        String bytecode = "";
        String localInstruction = "";
        String[] instructions = rawBytecode.split("\n");

        System.out.println("loadBytecode: Loading the function...");
        for (int i = 0; i < instructions.length; i++) {
            String[] instruction = instructions[i].trim().split(" ");

            if (arguments.containsKey(instruction[instruction.length - 1].substring(1))) {
                for (int j = 0; j < instruction.length - 1; j++) {
                    localInstruction += instruction[j] + " ";
                }
                localInstruction += arguments.get(instruction[instruction.length - 1].substring(1));
                bytecode += localInstruction + "\n";
                localInstruction = "";
            } else {
                bytecode += instructions[i].trim() + "\n";
            }
        }
        System.out.println("loadBytecode: Function loaded");
        System.out.println("loadBytecode: Function\n" + bytecode);

        return bytecode;
    }
}
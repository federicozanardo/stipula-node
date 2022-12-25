import messages.AgreementCallMessage;
import messages.Message;
import messages.SignedMessage;
import vm.VirtualMachine;
import vm.storage.GlobalStorage;
import vm.types.TraceChange;
import vm.types.address.Address;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.*;
import java.util.*;

import static lib.crypto.Crypto.generateKeyPair;
import static lib.crypto.Crypto.sign;

class Main {
    private static int offset = 0;

    public static void main(String[] args) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        File currentDirectory = new File(new File(".").getAbsolutePath());
        String path = currentDirectory + "/examples/";

        // Start example of signed message
        Base64.Encoder encoder = Base64.getEncoder();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // Generate keys
        KeyPair lenderKeys = generateKeyPair();
        KeyPair borrowerKeys = generateKeyPair();

        // Get public key as String
        String lenderPubKey = encoder.encodeToString(lenderKeys.getPublic().getEncoded());
        String borrowerPubKey = encoder.encodeToString(borrowerKeys.getPublic().getEncoded());

        // Set up the addresses
        Address lenderAddress = new Address(lenderPubKey);
        Address borrowerAddress = new Address(borrowerPubKey);

        // Load the parties' addresses
        HashMap<String, Address> parties = new HashMap<>();
        parties.put("Lender", lenderAddress);
        parties.put("Borrower", borrowerAddress);

        HashMap<String, String> argumentsMessage = new HashMap<>();
        argumentsMessage.put("cost", "12");
        argumentsMessage.put("rent_time", "1");

        AgreementCallMessage agreementCallMessage = new AgreementCallMessage("asd123", argumentsMessage, parties);
        Message message = agreementCallMessage;

        String lenderSign = sign(agreementCallMessage.toString(), lenderKeys.getPrivate());
        String borrowerSign = sign(agreementCallMessage.toString(), borrowerKeys.getPrivate());
        HashMap<String, String> signatures = new HashMap<>();
        signatures.put("Lender", lenderSign);
        signatures.put("Borrower", borrowerSign);

        SignedMessage signedMessage = new SignedMessage(agreementCallMessage, signatures);
        // End example of signed message

        // Load the function
        String rawBytecode = loadFunction(path + "contract1.sb");

        // Load arguments
        HashMap<String, String> arguments = loadArguments(message);

        // Load the bytecode
        String bytecode = loadBytecode(rawBytecode, arguments);
        String[] instructions = bytecode.split("\n");

        GlobalStorage globalStorage = new GlobalStorage();

        // Prepare the virtual machine
        VirtualMachine vm;

        if (message instanceof AgreementCallMessage) {
            // Execute the function
            vm = new VirtualMachine(instructions, offset);
        } else {
            // Load global storage
            HashMap<String, TraceChange> globalSpace = globalStorage.loadGlobalStorage();

            // Execute the function
            vm = new VirtualMachine(instructions, offset, globalSpace);
        }

        // Execute the code
        boolean result = vm.execute();

        if (!result) {
            System.out.println("main: Error while executing the function");
            return;
        }

        System.out.println("main: Updating the global store...");
        if (vm.getGlobalSpace().isEmpty()) {
            System.out.println("There is anything to save in the global space");
        } else {
            globalStorage.storeGlobalStorage(vm.getGlobalSpace());
            System.out.println("main: Global store updated");
        }
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

                if (data.startsWith("/*")) {
                    startMultilineComment = true;
                }

                if (data.startsWith("*/")) {
                    endMultilineComment = true;
                }

                if (data.startsWith("//")) {
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
                // bytecodeFunction += vm.instructions[i].trim() + "\n";
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
        String substitution = "";
        String[] instructions = rawBytecode.split("\n");

        System.out.println("loadBytecode: Loading the function...");
        for (int i = 0; i < instructions.length; i++) {
            String[] instruction = instructions[i].trim().split(" ");

            if (arguments.containsKey(instruction[instruction.length - 1].substring(1))) {
                for (int j = 0; j < instruction.length - 1; j++) {
                    substitution += instruction[j] + " ";
                }
                substitution += arguments.get(instruction[instruction.length - 1].substring(1));
                bytecode += substitution + "\n";
                substitution = "";
            } else {
                bytecode += instructions[i].trim() + "\n";
            }
        }
        System.out.println("loadBytecode: Function loaded");
        System.out.println("loadBytecode: Function\n" + bytecode);

        return bytecode;
    }
}
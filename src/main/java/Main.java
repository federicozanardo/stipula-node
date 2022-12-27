import constants.Constants;
import messages.AgreementCallMessage;
import messages.Message;
import messages.SignedMessage;
import vm.VirtualMachine;
import vm.contract.ContractInstance;
import vm.storage.GlobalStorage;
import vm.types.address.Address;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;

import static lib.crypto.Crypto.*;

class Main {
    private static int offset = 0;

    public static void main(String[] args) throws Exception {
        String path = String.valueOf(Constants.EXAMPLES_PATH);

        SignedMessage signedMessage = generateAgreementCallMessage(path);
        // SignedMessage signedMessage = generateFunctionCallMessage(path);
        Message message = signedMessage.getMessage();

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

        if (!(message instanceof AgreementCallMessage)) {
            vm = new VirtualMachine(instructions, offset);
        } else {
            // String contractInstanceId = "bb69e66e-6e8e-4791-b1f9-75d76d1638ff";
            String contractInstanceId = "04e8e716-44d8-4e47-ad6a-4c28e97c1e3d";

            // Load global storage
            System.out.println("main: Loading the contract instance...");
            globalStorage.loadGlobalStorage(contractInstanceId);
            System.out.println("main: Contract instance loaded");

            vm = new VirtualMachine(instructions, offset, globalStorage.getStorage());
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
            String contractId = "asd123";
            ContractInstance instance = globalStorage.storeGlobalStorage(contractId, vm.getGlobalSpace());
            System.out.println("main: Global store updated");
            System.out.println("main: Contract instance id: " + instance.getInstanceId());
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

    private static SignedMessage generateAgreementCallMessage(String path) throws Exception {
        // Start example of signed message
        Base64.Encoder encoder = Base64.getEncoder();

        // Generate keys
        // KeyPair lenderKeys = generateKeyPair();
        // KeyPair borrowerKeys = generateKeyPair();

        PublicKey lenderPublicKey = getPublicKeyFromFile(path + "lender-keys/publicKey");
        PrivateKey lenderPrivateKey = getPrivateKeyFromFile(path + "lender-keys/privateKey");

        PublicKey borrowerPublicKey = getPublicKeyFromFile(path + "borrower-keys/publicKey");
        PrivateKey borrowerPrivateKey = getPrivateKeyFromFile(path + "borrower-keys/privateKey");

        // Get public key as String
        // String lenderPubKey = encoder.encodeToString(lenderKeys.getPublic().getEncoded());
        String lenderPubKey = encoder.encodeToString(lenderPublicKey.getEncoded());
        // String borrowerPubKey = encoder.encodeToString(borrowerKeys.getPublic().getEncoded());
        String borrowerPubKey = encoder.encodeToString(borrowerPublicKey.getEncoded());

        // Set up the addresses
        Address lenderAddress = new Address(lenderPubKey); // ubL35Am7TimL5R4oMwm2OxgAYA3XT3BeeDE56oxqdLc=
        Address borrowerAddress = new Address(borrowerPubKey); // f3hVW1Amltnqe3KvOT00eT7AU23FAUKdgmCluZB+nss=

        // Load the parties' addresses
        HashMap<String, Address> parties = new HashMap<>();
        parties.put("Lender", lenderAddress);
        parties.put("Borrower", borrowerAddress);

        HashMap<String, String> argumentsMessage = new HashMap<>();
        argumentsMessage.put("cost", "12");
        argumentsMessage.put("rent_time", "1");

        AgreementCallMessage agreementCallMessage = new AgreementCallMessage("asd123", argumentsMessage, parties);

        // String lenderSign = sign(agreementCallMessage.toString(), lenderKeys.getPrivate());
        String lenderSign = sign(agreementCallMessage.toString(), lenderPrivateKey);
        // String borrowerSign = sign(agreementCallMessage.toString(), borrowerKeys.getPrivate());
        String borrowerSign = sign(agreementCallMessage.toString(), borrowerPrivateKey);
        HashMap<String, String> signatures = new HashMap<>();
        signatures.put("Lender", lenderSign);
        signatures.put("Borrower", borrowerSign);

        return new SignedMessage(agreementCallMessage, signatures);
    }

    private static SignedMessage generateFunctionCallMessage(String path) throws Exception {
        // Start example of signed message
        Base64.Encoder encoder = Base64.getEncoder();

        // Generate keys
        // KeyPair lenderKeys = generateKeyPair();
        // KeyPair borrowerKeys = generateKeyPair();

        PublicKey lenderPublicKey = getPublicKeyFromFile(path + "lender-keys/publicKey");
        PrivateKey lenderPrivateKey = getPrivateKeyFromFile(path + "lender-keys/privateKey");

        PublicKey borrowerPublicKey = getPublicKeyFromFile(path + "borrower-keys/publicKey");
        PrivateKey borrowerPrivateKey = getPrivateKeyFromFile(path + "borrower-keys/privateKey");

        // Get public key as String
        // String lenderPubKey = encoder.encodeToString(lenderKeys.getPublic().getEncoded());
        String lenderPubKey = encoder.encodeToString(lenderPublicKey.getEncoded());
        // String borrowerPubKey = encoder.encodeToString(borrowerKeys.getPublic().getEncoded());
        String borrowerPubKey = encoder.encodeToString(borrowerPublicKey.getEncoded());

        // Set up the addresses
        Address lenderAddress = new Address(lenderPubKey); // ubL35Am7TimL5R4oMwm2OxgAYA3XT3BeeDE56oxqdLc=
        Address borrowerAddress = new Address(borrowerPubKey); // f3hVW1Amltnqe3KvOT00eT7AU23FAUKdgmCluZB+nss=

        // Load the parties' addresses
        HashMap<String, Address> parties = new HashMap<>();
        parties.put("Lender", lenderAddress);
        parties.put("Borrower", borrowerAddress);

        HashMap<String, String> argumentsMessage = new HashMap<>();
        argumentsMessage.put("cost", "12");
        argumentsMessage.put("rent_time", "1");

        AgreementCallMessage agreementCallMessage = new AgreementCallMessage("asd123", argumentsMessage, parties);

        // String lenderSign = sign(agreementCallMessage.toString(), lenderKeys.getPrivate());
        String lenderSign = sign(agreementCallMessage.toString(), lenderPrivateKey);
        // String borrowerSign = sign(agreementCallMessage.toString(), borrowerKeys.getPrivate());
        String borrowerSign = sign(agreementCallMessage.toString(), borrowerPrivateKey);
        HashMap<String, String> signatures = new HashMap<>();
        signatures.put("Lender", lenderSign);
        signatures.put("Borrower", borrowerSign);

        return new SignedMessage(agreementCallMessage, signatures);
    }
}
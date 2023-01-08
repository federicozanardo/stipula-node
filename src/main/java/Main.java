import asset.FungibleAsset;
import constants.Constants;
import messages.Message;
import messages.SignedMessage;
import messages.agreement.AgreementCallMessage;
import messages.function.FunctionCallMessage;
import messages.function.PayToContract;
import vm.ScriptVirtualMachine;
import vm.SmartContractVirtualMachine;
import vm.contract.SingleUseSeal;
import vm.storage.ContractInstancesStorage;
import vm.types.AssetType;
import vm.types.FloatType;
import vm.types.TraceChange;
import vm.types.address.AddrType;
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

        // SignedMessage signedMessage = callAgreementFunction(path);
        // SignedMessage signedMessage = callOfferFunction(path);
        SignedMessage signedMessage = callAcceptFunction(path);
        // SignedMessage signedMessage = callEndFunction(path);
        Message message = signedMessage.getMessage();

        // Load the function
        String rawBytecode;
        // rawBytecode = loadFunction(path + "contract1.sb", "agreement");
        if (message instanceof AgreementCallMessage) {
            rawBytecode = loadFunction(path + "contract1.sb", "agreement");
        } else {
            FunctionCallMessage functionCallMessage = (FunctionCallMessage) message;
            rawBytecode = loadFunction(path + "contract1.sb", functionCallMessage.getFunction());
        }

        // Load arguments
        // HashMap<String, String> arguments = new HashMap<>();
        HashMap<String, String> arguments = loadArguments(message);

        HashMap<String, AssetType> assetArguments = new HashMap<>();
        if (message instanceof FunctionCallMessage) {
            assetArguments = loadAssetArguments(message);
        }

        // Load the bytecode
        String bytecode;
        if (message instanceof AgreementCallMessage) {
            bytecode = loadBytecode(rawBytecode, arguments);
        } else {
            bytecode = loadBytecode(rawBytecode, arguments, assetArguments);
        }
        String[] instructions = bytecode.split("\n");

        ContractInstancesStorage globalStorage = new ContractInstancesStorage();
        /*
        // Load the DFA
        Address lenderAddr = new Address("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCo/GjVKS+3gAA55+kko41yINdOcCLQMSBQyuTTkKHE1mhu/TgOpivM0wLPsSga8hQMr3+v3aR0IF/vfCRf6SdiXmWx/jflmEXtnT6fkGcnV6dGNUpHWXSpwUIDt0N88jfnEqekx4S+KDCKg99sGEeHeT65fKS8lB0gjHMt9AOriwIDAQAB");
        Address borrowerAddr = new Address("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDErzzgD2ZslZxciFAiX3/ot7lrkZDw4148jFZrsDZPE6CVs9xXFSHGgy/mFvIFLXhnChO6Nyd2be3lbgeavLMCMVUiTStXr117Km17keWpb3sItkKKsLFBOcIIU8XXowI/OhzQN2XPZYESHgjdQ5vwEj2YyueiS7WKP94YWz/pswIDAQAB");

        ArrayList<Address> authorizedParties1 = new ArrayList<Address>();
        authorizedParties1.add(lenderAddr);
        ArrayList<Address> authorizedParties2 = new ArrayList<Address>();
        authorizedParties2.add(borrowerAddr);

        ArrayList<Pair<String, State>> transitions = new ArrayList<>();
        transitions.add(new Pair<String, State>("Inactive", new ContractCallByParty("Proposal", authorizedParties1)));
        transitions.add(new Pair<String, State>("Proposal", new ContractCallByParty("Using", authorizedParties2)));
        transitions.add(new Pair<String, State>("Using", new ContractCallByParty("End", authorizedParties2)));

        DeterministicFiniteAutomata dfa = new DeterministicFiniteAutomata("Inactive", "End", transitions);
        if (!dfa.isNextState("Proposal", lenderAddr)) {
            // Error
            System.out.println("main: Error in the state machine");
        }*/

        // Prepare the virtual machine
        SmartContractVirtualMachine vm;

        String contractId = "asd123";
        // String contractInstanceId = "bb69e66e-6e8e-4791-b1f9-75d76d1638ff";
        String contractInstanceId = "04e8e716-44d8-4e47-ad6a-4c28e97c1e3d";

        if (message instanceof AgreementCallMessage) {
            vm = new SmartContractVirtualMachine(instructions, offset);
        } else {
            // Load global storage
            System.out.println("main: Loading the contract instance...");
            // globalStorage.loadGlobalStorage(contractInstanceId);
            HashMap<String, TraceChange> global = new HashMap<>();
            global.put("cost", new TraceChange(new FloatType(300, 2), false));
            // global.put("wallet", new TraceChange(new AssetType("iop890", new FloatType(0, 2)), false));
            global.put("wallet", new TraceChange(new AssetType("iop890", new FloatType(300, 2)), false));
            Base64.Encoder encoder = Base64.getEncoder();
            global.put("Lender", new TraceChange(
                    new AddrType(
                            new Address(
                                    encoder.encodeToString(getPublicKeyFromFile(path + "lender-keys/publicKey").getEncoded())
                            )
                    ),
                    false)
            );
            // System.out.println(global);
            System.out.println("main: Contract instance loaded");

            vm = new SmartContractVirtualMachine(instructions, offset, global);
            // vm = new VirtualMachine(instructions, offset, globalStorage.getStorage());
            // vm = new VirtualMachine(instructions, offset);
        }

        // Execute the code
        boolean result = vm.execute();

        if (!result) {
            System.out.println("main: Error while executing the function");
            return;
        }

        // Go to the next state
        /*dfa.nextState("Proposal", lenderAddr);

        System.out.println("main: current state = " + dfa.getCurrentState());

        System.out.println("main: Updating the global store...");
        if (vm.getGlobalSpace().isEmpty()) {
            System.out.println("There is anything to save in the global space");
        } else {
            if (message instanceof AgreementCallMessage) {
                ContractInstance instance = globalStorage.storeGlobalStorage(contractId, vm.getGlobalSpace());
                System.out.println("main: Contract instance id: " + instance.getInstanceId());
            } else {
                // globalStorage.storeGlobalStorage(contractId, vm.getGlobalSpace());
                ContractInstance instance = globalStorage.getContractInstance(contractInstanceId);
                globalStorage.storeGlobalStorage(vm.getGlobalSpace(), instance);

                *//*for (HashMap.Entry<String, TraceChange> entry : globalStorage.getStorage().entrySet()) {
                    System.out.println(asString(bytes(entry.getKey())) + ": " + entry.getValue().getValue());
                    this.storage.put(entry.getKey(), new TraceChange(entry.getValue()));
                }*//*
            }

            System.out.println("main: Global store updated");
        }*/
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

    private static String loadFunction(String pathname, String function) {
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

            if (instruction[0].equals("fn") && instruction[1].equals(function)) {
                isFunctionStarted = true;
                offset = i + 1;
            }

            if (instruction[0].equals("HALT") && isFunctionStarted) {
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
            AgreementCallMessage agreementCallMessage = (AgreementCallMessage) message;
            arguments.putAll(agreementCallMessage.getArguments());

            for (HashMap.Entry<String, Address> entry : agreementCallMessage.getParties().entrySet()) {
                arguments.put(entry.getKey(), entry.getValue().getPublicKey());
            }
        } else {
            FunctionCallMessage functionCallMessage = (FunctionCallMessage) message;
            arguments.putAll(functionCallMessage.getArguments());
        }

        return arguments;
    }

    private static HashMap<String, AssetType> loadAssetArguments(Message message) throws Exception {
        HashMap<String, AssetType> assetArguments = new HashMap<>();

        if (message instanceof FunctionCallMessage) {
            FunctionCallMessage functionCallMessage = (FunctionCallMessage) message;

            if (!functionCallMessage.getAssetArguments().isEmpty()) {
                for (HashMap.Entry<String, PayToContract> entry : functionCallMessage.getAssetArguments().entrySet()) {
                    // Set up the asset
                    FungibleAsset bitcoin = new FungibleAsset("iop890", "Bitcoin", "BTC", 10000, 2);

                    PayToContract payToContract = entry.getValue();
                    SingleUseSeal singleUseSeal = payToContract.getSingleUseSeal();

                    // TODO: Check if the single-use seal exists
                    // singleUseSeal.getId()

                    // Check if the asset id matches
                    if (!singleUseSeal.getAssetId().equals(bitcoin.getAssetId())) {
                        // Error
                    }

                    // Check if the decimals matches
                    if (!(singleUseSeal.getAmount().getDecimals() == bitcoin.getDecimals())) {
                        // Error
                    }

                    // Check if the amount <= asset supply
                    if (!(singleUseSeal.getAmount().getInteger() <= bitcoin.getSupply())) {
                        // Error
                    }

                    // Check if it is possible to unlock the script
                    String script = payToContract.getUnlockScript() + singleUseSeal.getLockScript();
                    System.out.println("loadAssetArguments: Script to validate\n" + script);
                    String[] instructions = script.split("\n");

                    System.out.println("loadAssetArguments: Start validating the script...");
                    ScriptVirtualMachine vm = new ScriptVirtualMachine(instructions);

                    // Execute the code
                    boolean result = vm.execute();

                    if (!result) {
                        System.out.println("loadAssetArguments: Error while executing the function");
                        return null;
                    }

                    System.out.println("loadAssetArguments: Script validated");

                    AssetType value = new AssetType(
                            bitcoin.getAssetId(),
                            new FloatType(
                                    singleUseSeal.getAmount().getInteger(),
                                    singleUseSeal.getAmount().getDecimals()
                            )
                    );

                    // All the checks are true, so add the argument
                    assetArguments.put(entry.getKey(), value);
                }
            }

            return assetArguments;
        } else {
            // Error
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

    private static String loadBytecode(String rawBytecode, HashMap<String, String> arguments, HashMap<String, AssetType> assetArguments) {
        String bytecode = loadBytecode(rawBytecode, arguments);
        String finalBytecode = "";
        String substitution = "";
        String[] instructions = bytecode.split("\n");

        System.out.println("loadBytecode: Loading the function...");
        for (int i = 0; i < instructions.length; i++) {
            String[] instruction = instructions[i].trim().split(" ");

            if (assetArguments.containsKey(instruction[instruction.length - 1].substring(1))) {
                for (int j = 0; j < instruction.length - 1; j++) {
                    substitution += instruction[j] + " ";
                }
                AssetType value = assetArguments.get(instruction[instruction.length - 1].substring(1));
                substitution += value.getValue().getInteger() + " " + value.getValue().getDecimals();
                finalBytecode += substitution + "\n";
                substitution = "";
            } else {
                finalBytecode += instructions[i].trim() + "\n";
            }
        }
        System.out.println("loadBytecode: Function loaded");
        System.out.println("loadBytecode: Function\n" + finalBytecode);

        return finalBytecode;
    }

    private static SignedMessage callAgreementFunction(String path) throws Exception {
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

        HashMap<String, String> arguments = new HashMap<>();
        // arguments.put("cost", "12");
        arguments.put("cost", "1200 2");
        arguments.put("rent_time", "1");

        AgreementCallMessage agreementCallMessage = new AgreementCallMessage(
                "asd123",
                arguments,
                parties);

        // String lenderSign = sign(agreementCallMessage.toString(), lenderKeys.getPrivate());
        String lenderSign = sign(agreementCallMessage.toString(), lenderPrivateKey);
        // String borrowerSign = sign(agreementCallMessage.toString(), borrowerKeys.getPrivate());
        String borrowerSign = sign(agreementCallMessage.toString(), borrowerPrivateKey);
        HashMap<String, String> signatures = new HashMap<>();
        signatures.put("Lender", lenderSign);
        signatures.put("Borrower", borrowerSign);

        return new SignedMessage(agreementCallMessage, signatures);
    }

    private static SignedMessage callOfferFunction(String path) throws Exception {
        PrivateKey lenderPrivateKey = getPrivateKeyFromFile(path + "lender-keys/privateKey");

        HashMap<String, String> arguments = new HashMap<>();
        arguments.put("z", "1");

        FunctionCallMessage functionCallMessage = new FunctionCallMessage(
                "asd123",
                "04e8e716-44d8-4e47-ad6a-4c28e97c1e3d",
                "offer");
        functionCallMessage.setArguments(arguments);

        String lenderSign = sign(functionCallMessage.toString(), lenderPrivateKey);
        HashMap<String, String> signatures = new HashMap<>();
        signatures.put("Lender", lenderSign);

        return new SignedMessage(functionCallMessage, signatures);
    }

    private static SignedMessage callAcceptFunction(String path) throws Exception {
        Base64.Encoder encoder = Base64.getEncoder();

        PublicKey borrowerPublicKey = getPublicKeyFromFile(path + "borrower-keys/publicKey");
        PrivateKey borrowerPrivateKey = getPrivateKeyFromFile(path + "borrower-keys/privateKey");

        // Get public key as String
        String borrowerPubKey = encoder.encodeToString(borrowerPublicKey.getEncoded());

        // Set up the address
        Address borrowerAddress = new Address(borrowerPubKey);

        HashMap<String, String> arguments = new HashMap<>();

        // Set up the asset
        FungibleAsset bitcoin = new FungibleAsset("iop890", "Bitcoin", "BTC", 10000, 2);

        // Set up the single-use seal
        FloatType amount = new FloatType(300, 2);
        SingleUseSeal singleUseSeal = new SingleUseSeal("aaa111", bitcoin.getAssetId(), amount, borrowerAddress.getAddress());

        // Set up the unlock script
        String signature = sign("aaa111", borrowerPrivateKey);
        String unlockScript = "PUSH str " + signature + "\nPUSH str " + borrowerPubKey + "\n";

        PayToContract payToContract = new PayToContract(
                /*"04e8e716-44d8-4e47-ad6a-4c28e97c1e3d",*/
                singleUseSeal,
                unlockScript
        );

        HashMap<String, PayToContract> assetArguments = new HashMap<>();
        assetArguments.put("y", payToContract);

        FunctionCallMessage functionCallMessage = new FunctionCallMessage(
                "asd123",
                "dsa321",
                "accept");
        functionCallMessage.setArguments(arguments);
        functionCallMessage.setAssetArguments(assetArguments);

        String borrowerSign = sign(functionCallMessage.toString(), borrowerPrivateKey);
        HashMap<String, String> signatures = new HashMap<>();
        signatures.put("Borrower", borrowerSign);

        return new SignedMessage(functionCallMessage, signatures);
    }

    private static SignedMessage callEndFunction(String path) throws Exception {
        PrivateKey borrowerPrivateKey = getPrivateKeyFromFile(path + "borrower-keys/privateKey");

        FunctionCallMessage functionCallMessage = new FunctionCallMessage(
                "asd123",
                "04e8e716-44d8-4e47-ad6a-4c28e97c1e3d",
                "end");

        String borrowerSign = sign(functionCallMessage.toString(), borrowerPrivateKey);
        HashMap<String, String> signatures = new HashMap<>();
        signatures.put("Borrower", borrowerSign);

        return new SignedMessage(functionCallMessage, signatures);
    }
}
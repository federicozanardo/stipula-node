import asset.FungibleAsset;
import constants.Constants;
import lib.datastructures.Pair;
import messages.Message;
import messages.SignedMessage;
import messages.agreement.AgreementCallMessage;
import messages.function.FunctionCallMessage;
import messages.function.PayToContract;
import org.iq80.leveldb.DBIterator;
import vm.ScriptVirtualMachine;
import vm.SmartContractVirtualMachine;
import vm.contract.Contract;
import vm.contract.ContractInstance;
import vm.contract.SingleUseSeal;
import vm.dfa.ContractCallByParty;
import vm.dfa.DeterministicFiniteAutomata;
import vm.dfa.State;
import vm.storage.ContractInstancesStorage;
import vm.storage.ContractsStorage;
import vm.types.AssetType;
import vm.types.FloatType;
import vm.types.TraceChange;
import vm.types.Type;
import vm.types.address.AddrType;
import vm.types.address.Address;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

import static lib.crypto.Crypto.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

class Main {
    private static int offset = 0;
    private static ContractsStorage contractsStorage;

    public static void main(String[] args) throws Exception {
        String path = String.valueOf(Constants.EXAMPLES_PATH);

        contractsStorage = new ContractsStorage();
        ContractInstancesStorage contractInstancesStorage = new ContractInstancesStorage();

//         setupContract();
//         System.exit(0);

        // SignedMessage signedMessage = callAgreementFunction(path);
        // SignedMessage signedMessage = callOfferFunction(path);
        // SignedMessage signedMessage = callAcceptFunction(path);
        SignedMessage signedMessage = callEndFunction(path);
        Message message = signedMessage.getMessage();

        Contract contract;
        ContractInstance instance;
        String nextState = "";
        Address address = null;

        // Load the function
        String rawBytecode;
        if (message instanceof AgreementCallMessage) {
            AgreementCallMessage agreementCallMessage = (AgreementCallMessage) message;

            // Get the contract
            contract = contractsStorage.getContract(agreementCallMessage.getContractId());

            DeterministicFiniteAutomata stateMachine = new DeterministicFiniteAutomata(
                    contract.getInitialState(),
                    contract.getEndState(),
                    contract.getTransitions()
            );

            // Create a new instance of the contract
            instance = new ContractInstance(
                    agreementCallMessage.getContractId(),
                    stateMachine
            );
            contractInstancesStorage.createContractInstance(instance);
            System.out.println("main: contractInstanceId = " + instance.getInstanceId());

            rawBytecode = loadFunction(agreementCallMessage.getContractId(), "agreement");
            System.out.println("main: loadFunction\n" + rawBytecode);
        } else {
            FunctionCallMessage functionCallMessage = (FunctionCallMessage) message;

            if (signedMessage.getSignatures().size() == 0) {
                // Error
            }

            if (signedMessage.getSignatures().size() > 1) {
                // Error
            }

            Map.Entry<String, String> first = signedMessage.getSignatures().entrySet().iterator().next();
            String publicKeyParty = first.getKey();

            address = new Address(publicKeyParty);

            instance = contractInstancesStorage.getContractInstance(functionCallMessage.getContractInstanceId());

            nextState = getNextStateFromFunction(functionCallMessage.getContractId(), functionCallMessage.getFunction());
            System.out.println("main: nextState: " + nextState);

            rawBytecode = loadFunction(functionCallMessage.getContractId(), functionCallMessage.getFunction());
            System.out.println("loadFunction: Function\n" + rawBytecode);
        }

        // Load arguments
        HashMap<String, String> arguments = loadArguments(message);

        // Load asset arguments
        HashMap<String, AssetType> assetArguments = new HashMap<>();
        if (message instanceof FunctionCallMessage) {
            assetArguments = loadAssetArguments(message);
        }

        // Load the bytecode
        String bytecode;
        if (message instanceof AgreementCallMessage) {
            bytecode = loadBytecode(rawBytecode, arguments);
            System.out.println("main: loadBytecode\n" + bytecode);
        } else {
            bytecode = loadBytecode(rawBytecode, arguments, assetArguments);
            System.out.println("main: loadBytecode\n" + bytecode);
        }

        String[] instructions = bytecode.split("\n");

        // Check if the state is correct
        if (message instanceof FunctionCallMessage && !instance.getStateMachine().isNextState(nextState, address)) {
            // Error
            System.out.println("main: Error in the state machine");
            System.out.println("main: Current state => " + instance.getStateMachine().getCurrentState());
            System.out.println("main: Next state => " + nextState);
            System.out.println(address);
            System.exit(-1);
        }

        // Set up the virtual machine
        SmartContractVirtualMachine vm;

        if (message instanceof AgreementCallMessage) {
            vm = new SmartContractVirtualMachine(instructions, offset);
        } else {
            // Load global storage
            HashMap<String, TraceChange> global = new HashMap<>();

            for (HashMap.Entry<String, Type> entry : instance.getGlobalVariables().entrySet()) {
                global.put(entry.getKey(), new TraceChange(entry.getValue()));
            }

            vm = new SmartContractVirtualMachine(instructions, offset, global);
        }

        // Execute the code
        boolean result = vm.execute();

        if (!result) {
            System.out.println("main: Error while executing the function");
            return;
        }

        // TODO: Go to the next state
        instance.getStateMachine().nextState(nextState, address);
        System.out.println("main: current state = " + instance.getStateMachine().getCurrentState());

        System.out.println("main: Updating the global store...");
        if (vm.getGlobalSpace().isEmpty()) {
            System.out.println("There is nothing to save in the global store");
        } else {
            contractInstancesStorage.storeGlobalStorage(vm.getGlobalSpace(), instance);
            System.out.println("main: Global store updated");
        }

        // contractInstancesStorage.close();

        System.out.println("GLOBALSPACE");
        System.out.println(instance.getGlobalVariables());
        System.out.println(contractInstancesStorage.getContractInstance(instance.getInstanceId()).getGlobalVariables());
    }

    private static void setupContract() throws IOException, NoSuchAlgorithmException {
        String bytecode = readProgram(Constants.EXAMPLES_PATH + "contract1.sb");

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

        Contract contract = new Contract("", bytecode, "Inactive", "End", transitions);

        // Save the contract
        String contractId = contractsStorage.addContract(contract);

        System.out.println("setupContract: contractId = " + contractId);
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

    private static String getNextStateFromFunction(String contractId, String function) throws IOException {
        String nextState = "";

        // Load all the bytecode
        // String bytecode = readProgram(pathname);
        Contract contract = contractsStorage.getContract(contractId);
        String bytecode = contract.getBytecode();
        String[] instructions = bytecode.split("\n");

        System.out.println("loadFunction: Loading the function...");
        for (String s : instructions) {
            String[] instruction = s.trim().split(" ");

            if (instruction[0].equals("fn") && instruction[1].equals(function)) {
                nextState = instruction[3];
            }
        }
        System.out.println("loadFunction: Function loaded");

        return nextState;
    }

    private static String loadFunction(String contractId, String function) throws IOException {
        String bytecodeFunction = "";
        boolean isFunctionStarted = false;
        boolean isFunctionEnded = false;

        // Load all the bytecode
        // String bytecode = readProgram(pathname);
        Contract contract = contractsStorage.getContract(contractId);
        String bytecode = contract.getBytecode();
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
        System.out.println("loadBytecode: Function loaded\n");

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
                "ad01c9f1-b255-495c-9801-a56fb651bb53",
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
                "ad01c9f1-b255-495c-9801-a56fb651bb53",
                "5d28d2b4-cc25-40c8-861e-3b174308ece3",
                "offer");
        functionCallMessage.setArguments(arguments);

        String lenderSign = sign(functionCallMessage.toString(), lenderPrivateKey);
        HashMap<String, String> signatures = new HashMap<>();
        // signatures.put("Lender", lenderSign);
        signatures.put(
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCo/GjVKS+3gAA55+kko41yINdOcCLQMSBQyuTTkKHE1mhu/TgOpivM0wLPsSga8hQMr3+v3aR0IF/vfCRf6SdiXmWx/jflmEXtnT6fkGcnV6dGNUpHWXSpwUIDt0N88jfnEqekx4S+KDCKg99sGEeHeT65fKS8lB0gjHMt9AOriwIDAQAB",
                lenderSign);

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
        FungibleAsset bitcoin = new FungibleAsset("iop890", "Bitcoin", "BTC", 100000, 2);

        // Set up the single-use seal
        FloatType amount = new FloatType(1200, 2);
        SingleUseSeal singleUseSeal = new SingleUseSeal("aaa111", bitcoin.getAssetId(), amount, borrowerAddress.getAddress());

        // Set up the unlock script
        String signature = sign("aaa111", borrowerPrivateKey);
        String unlockScript = "PUSH str " + signature + "\nPUSH str " + borrowerPubKey + "\n";

        PayToContract payToContract = new PayToContract(
                singleUseSeal,
                unlockScript
        );

        HashMap<String, PayToContract> assetArguments = new HashMap<>();
        assetArguments.put("y", payToContract);

        FunctionCallMessage functionCallMessage = new FunctionCallMessage(
                "ad01c9f1-b255-495c-9801-a56fb651bb53",
                "5d28d2b4-cc25-40c8-861e-3b174308ece3",
                "accept");
        functionCallMessage.setArguments(arguments);
        functionCallMessage.setAssetArguments(assetArguments);

        String borrowerSign = sign(functionCallMessage.toString(), borrowerPrivateKey);
        HashMap<String, String> signatures = new HashMap<>();
        signatures.put(
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDErzzgD2ZslZxciFAiX3/ot7lrkZDw4148jFZrsDZPE6CVs9xXFSHGgy/mFvIFLXhnChO6Nyd2be3lbgeavLMCMVUiTStXr117Km17keWpb3sItkKKsLFBOcIIU8XXowI/OhzQN2XPZYESHgjdQ5vwEj2YyueiS7WKP94YWz/pswIDAQAB",
                borrowerSign);

        return new SignedMessage(functionCallMessage, signatures);
    }

    private static SignedMessage callEndFunction(String path) throws Exception {
        PrivateKey borrowerPrivateKey = getPrivateKeyFromFile(path + "borrower-keys/privateKey");

        FunctionCallMessage functionCallMessage = new FunctionCallMessage(
                "ad01c9f1-b255-495c-9801-a56fb651bb53",
                "5d28d2b4-cc25-40c8-861e-3b174308ece3",
                "end");

        String borrowerSign = sign(functionCallMessage.toString(), borrowerPrivateKey);
        HashMap<String, String> signatures = new HashMap<>();
        signatures.put(
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDErzzgD2ZslZxciFAiX3/ot7lrkZDw4148jFZrsDZPE6CVs9xXFSHGgy/mFvIFLXhnChO6Nyd2be3lbgeavLMCMVUiTStXr117Km17keWpb3sItkKKsLFBOcIIU8XXowI/OhzQN2XPZYESHgjdQ5vwEj2YyueiS7WKP94YWz/pswIDAQAB",
                borrowerSign);

        return new SignedMessage(functionCallMessage, signatures);
    }
}
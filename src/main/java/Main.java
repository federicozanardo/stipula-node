import event.EventTriggerHandler;
import models.address.Address;
import models.dto.requests.Message;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.requests.contract.function.FunctionCall;
import models.dto.responses.Response;
import server.MessageServer;
import shared.SharedMemory;
import storage.ContractInstancesStorage;
import storage.ContractsStorage;
import vm.RequestQueue;
import vm.VirtualMachine;
import vm.types.AssetType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

class Main {
    /*public static void main(String[] args) {
        RequestQueue requestQueue = new RequestQueue();
        Thread thread1 = new Thread("thread1") {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                System.out.println(this.getName() + ": I'm waiting to push in the queue...");
                try {
                    requestQueue.enqueue(this.getName());
                } catch (QueueOverflowException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(this.getName() + ": Pushed in the queue");
            }
        };

        Thread thread2 = new Thread("thread2") {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                System.out.println(this.getName() + ": I'm waiting to push in the queue...");
                try {
                    requestQueue.enqueue(this.getName());
                } catch (QueueOverflowException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(this.getName() + ": Pushed in the queue");
            }
        };

        Thread thread3 = new Thread("thread3") {
            @Override
            public void run() {
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                System.out.println(this.getName() + ": I'm waiting to push in the queue...");
                try {
                    requestQueue.enqueue(this.getName());
                } catch (QueueOverflowException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(this.getName() + ": Pushed in the queue");
            }
        };

        thread1.start();
        thread2.start();
        thread3.start();
    }*/

    public static void main(String[] args) {
        // Set up the requests queue
        RequestQueue requestQueue = new RequestQueue();

        //
        // StorageRequestQueue storageRequestQueue = new StorageRequestQueue();

        // HashMap<String, Response> responsesToSend = new HashMap<>();
        SharedMemory<Response> sharedMemory = new SharedMemory<>();

        //
        // Storage storage = new Storage(storageRequestQueue, sharedMemory);

        // Set up the storage
        ContractsStorage contractsStorage = new ContractsStorage();
        ContractInstancesStorage contractInstancesStorage = new ContractInstancesStorage();
        // TODO: asset storage

        // Set up the virtual machine handler
        VirtualMachine virtualMachine = new VirtualMachine(
                requestQueue,
                sharedMemory,
                contractsStorage,
                contractInstancesStorage
        );

        // Set up the Event trigger handler
        EventTriggerHandler eventTriggerHandler = new EventTriggerHandler(requestQueue, virtualMachine);

        // Set up the server
        Thread server = new Thread(
                new MessageServer(
                        61000,
                        requestQueue,
                        eventTriggerHandler,
                        virtualMachine,
                        sharedMemory,
                        contractsStorage),
                "Message server"
        );

        //
        // storage.start();

        // Start the virtual machine
        virtualMachine.start();

        // Start the server
        server.start();
    }

    /*public static void oldMain(String[] args) throws Exception {
        String path = String.valueOf(Constants.EXAMPLES_PATH);

        contractsStorage = new ContractsStorage();
        ContractInstancesStorage contractInstancesStorage = new ContractInstancesStorage();

        // setupContract();
        // System.exit(0);

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
        if (message instanceof AgreementCall) {
            AgreementCall agreementCall = (AgreementCall) message;

            // Get the contract
            contract = contractsStorage.getContract(agreementCall.getContractId());

            DeterministicFiniteAutomata stateMachine = new DeterministicFiniteAutomata(
                    contract.getInitialState(),
                    contract.getEndState(),
                    contract.getTransitions()
            );

            // Create a new instance of the contract
            instance = new ContractInstance(
                    agreementCall.getContractId(),
                    stateMachine
            );
            contractInstancesStorage.createContractInstance(instance);
            System.out.println("main: contractInstanceId = " + instance.getInstanceId());

            rawBytecode = loadFunction(agreementCall.getContractId(), "agreement");
            System.out.println("main: loadFunction\n" + rawBytecode);
        } else {
            FunctionCall functionCall = (FunctionCall) message;

            if (signedMessage.getSignatures().size() == 0) {
                // Error
            }

            if (signedMessage.getSignatures().size() > 1) {
                // Error
            }

            Map.Entry<String, String> first = signedMessage.getSignatures().entrySet().iterator().next();
            String publicKeyParty = first.getKey();

            address = new Address(publicKeyParty);

            instance = contractInstancesStorage.getContractInstance(functionCall.getContractInstanceId());

            nextState = getNextStateFromFunction(functionCall.getContractId(), functionCall.getFunction());
            System.out.println("main: nextState: " + nextState);

            rawBytecode = loadFunction(functionCall.getContractId(), functionCall.getFunction());
            System.out.println("loadFunction: Function\n" + rawBytecode);
        }

        // Load arguments
        HashMap<String, String> arguments = loadArguments(message);

        // Load asset arguments
        HashMap<String, AssetType> assetArguments = new HashMap<>();
        if (message instanceof FunctionCall) {
            assetArguments = loadAssetArguments(message);
        }

        // Load the bytecode
        String bytecode;
        if (message instanceof AgreementCall) {
            bytecode = loadBytecode(rawBytecode, arguments);
            System.out.println("main: loadBytecode\n" + bytecode);
        } else {
            bytecode = loadBytecode(rawBytecode, arguments, assetArguments);
            System.out.println("main: loadBytecode\n" + bytecode);
        }

        String[] instructions = bytecode.split("\n");

        // Check if the state is correct
        if (message instanceof FunctionCall && !instance.getStateMachine().isNextState(nextState, address)) {
            // Error
            System.out.println("main: Error in the state machine");
            System.out.println("main: Current state => " + instance.getStateMachine().getCurrentState());
            System.out.println("main: Next state => " + nextState);
            System.out.println(address);
            System.exit(-1);
        }

        // Set up the virtual machine
        SmartContractVirtualMachine vm;

        if (message instanceof AgreementCall) {
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

        // Go to the next state
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
    }*/

    /*private static void setupContract() throws IOException, NoSuchAlgorithmException {
        String bytecode = readProgram(Constants.EXAMPLES_PATH + "contract1.sb");

        // Load the DFA
        Address lenderAddr = new Address("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCo/GjVKS+3gAA55+kko41yINdOcCLQMSBQyuTTkKHE1mhu/TgOpivM0wLPsSga8hQMr3+v3aR0IF/vfCRf6SdiXmWx/jflmEXtnT6fkGcnV6dGNUpHWXSpwUIDt0N88jfnEqekx4S+KDCKg99sGEeHeT65fKS8lB0gjHMt9AOriwIDAQAB");
        Address borrowerAddr = new Address("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDErzzgD2ZslZxciFAiX3/ot7lrkZDw4148jFZrsDZPE6CVs9xXFSHGgy/mFvIFLXhnChO6Nyd2be3lbgeavLMCMVUiTStXr117Km17keWpb3sItkKKsLFBOcIIU8XXowI/OhzQN2XPZYESHgjdQ5vwEj2YyueiS7WKP94YWz/pswIDAQAB");

        ArrayList<Address> authorizedParties1 = new ArrayList<Address>();
        authorizedParties1.add(lenderAddr);
        ArrayList<Address> authorizedParties2 = new ArrayList<Address>();
        authorizedParties2.add(borrowerAddr);

        ArrayList<Pair<String, DfaState>> transitions = new ArrayList<>();
        transitions.add(new Pair<String, DfaState>("Inactive", new ContractCallByParty("Proposal", authorizedParties1)));
        transitions.add(new Pair<String, DfaState>("Proposal", new ContractCallByParty("Using", authorizedParties2)));
        transitions.add(new Pair<String, DfaState>("Using", new ContractCallByParty("End", authorizedParties2)));

        Contract contract = new Contract("", bytecode, "Inactive", "End", transitions);

        // Save the contract
        String contractId = contractsStorage.addContract(contract);

        System.out.println("setupContract: contractId = " + contractId);
    }*/

    /*public static SignedMessage callAgreementFunction(String path) throws Exception {
        // Start example of signed message
        Base64.Encoder encoder = Base64.getEncoder();

        // Generate keys
        // KeyPair lenderKeys = generateKeyPair();
        // KeyPair borrowerKeys = generateKeyPair();

        PublicKey lenderPublicKey = Crypto.getPublicKeyFromFile(path + "lender-keys/publicKey");
        PrivateKey lenderPrivateKey = Crypto.getPrivateKeyFromFile(path + "lender-keys/privateKey");

        PublicKey borrowerPublicKey = Crypto.getPublicKeyFromFile(path + "borrower-keys/publicKey");
        PrivateKey borrowerPrivateKey = Crypto.getPrivateKeyFromFile(path + "borrower-keys/privateKey");

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

        AgreementCall agreementCall = new AgreementCall(
                "1dc563ca-066b-4bd1-9af3-399af8879a1d",
                arguments,
                parties
        );

        // String lenderSign = sign(agreementCallMessage.toString(), lenderKeys.getPrivate());
        String lenderSign = Crypto.sign(agreementCall.toString(), lenderPrivateKey);
        // String borrowerSign = sign(agreementCallMessage.toString(), borrowerKeys.getPrivate());
        String borrowerSign = Crypto.sign(agreementCall.toString(), borrowerPrivateKey);
        HashMap<String, String> signatures = new HashMap<>();
        signatures.put("Lender", lenderSign);
        signatures.put("Borrower", borrowerSign);

        return new SignedMessage(agreementCall, signatures);
    }

    private static SignedMessage callOfferFunction(String path) throws Exception {
        PrivateKey lenderPrivateKey = Crypto.getPrivateKeyFromFile(path + "lender-keys/privateKey");

        HashMap<String, String> arguments = new HashMap<>();
        arguments.put("z", "1");

        FunctionCall functionCall = new FunctionCall(
                "1dc563ca-066b-4bd1-9af3-399af8879a1d",
                "f017c131-8f66-4769-bcd2-1865c30e8510",
                "offer"
        );
        functionCall.setArguments(arguments);

        String lenderSign = Crypto.sign(functionCall.toString(), lenderPrivateKey);
        HashMap<String, String> signatures = new HashMap<>();
        // signatures.put("Lender", lenderSign);
        signatures.put(
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCo/GjVKS+3gAA55+kko41yINdOcCLQMSBQyuTTkKHE1mhu/TgOpivM0wLPsSga8hQMr3+v3aR0IF/vfCRf6SdiXmWx/jflmEXtnT6fkGcnV6dGNUpHWXSpwUIDt0N88jfnEqekx4S+KDCKg99sGEeHeT65fKS8lB0gjHMt9AOriwIDAQAB",
                lenderSign);

        return new SignedMessage(functionCall, signatures);
    }

    private static SignedMessage callAcceptFunction(String path) throws Exception {
        Base64.Encoder encoder = Base64.getEncoder();

        PublicKey borrowerPublicKey = Crypto.getPublicKeyFromFile(path + "borrower-keys/publicKey");
        PrivateKey borrowerPrivateKey = Crypto.getPrivateKeyFromFile(path + "borrower-keys/privateKey");

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
        String signature = Crypto.sign("aaa111", borrowerPrivateKey);
        String unlockScript = "PUSH str " + signature + "\nPUSH str " + borrowerPubKey + "\n";

        PayToContract payToContract = new PayToContract(
                singleUseSeal,
                unlockScript
        );

        HashMap<String, PayToContract> assetArguments = new HashMap<>();
        assetArguments.put("y", payToContract);

        FunctionCall functionCall = new FunctionCall(
                "1dc563ca-066b-4bd1-9af3-399af8879a1d",
                "f017c131-8f66-4769-bcd2-1865c30e8510",
                "accept"
        );
        functionCall.setArguments(arguments);
        functionCall.setAssetArguments(assetArguments);

        String borrowerSign = Crypto.sign(functionCall.toString(), borrowerPrivateKey);
        HashMap<String, String> signatures = new HashMap<>();
        signatures.put(
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDErzzgD2ZslZxciFAiX3/ot7lrkZDw4148jFZrsDZPE6CVs9xXFSHGgy/mFvIFLXhnChO6Nyd2be3lbgeavLMCMVUiTStXr117Km17keWpb3sItkKKsLFBOcIIU8XXowI/OhzQN2XPZYESHgjdQ5vwEj2YyueiS7WKP94YWz/pswIDAQAB",
                borrowerSign);

        return new SignedMessage(functionCall, signatures);
    }

    private static SignedMessage callEndFunction(String path) throws Exception {
        PrivateKey borrowerPrivateKey = Crypto.getPrivateKeyFromFile(path + "borrower-keys/privateKey");

        FunctionCall functionCall = new FunctionCall(
                "1dc563ca-066b-4bd1-9af3-399af8879a1d",
                "f017c131-8f66-4769-bcd2-1865c30e8510",
                "end"
        );

        String borrowerSign = Crypto.sign(functionCall.toString(), borrowerPrivateKey);
        HashMap<String, String> signatures = new HashMap<>();
        signatures.put(
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDErzzgD2ZslZxciFAiX3/ot7lrkZDw4148jFZrsDZPE6CVs9xXFSHGgy/mFvIFLXhnChO6Nyd2be3lbgeavLMCMVUiTStXr117Km17keWpb3sItkKKsLFBOcIIU8XXowI/OhzQN2XPZYESHgjdQ5vwEj2YyueiS7WKP94YWz/pswIDAQAB",
                borrowerSign);

        return new SignedMessage(functionCall, signatures);
    }*/
}
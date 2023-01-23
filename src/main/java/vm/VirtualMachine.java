package vm;

import exceptions.queue.QueueOverflowException;
import exceptions.queue.QueueUnderflowException;
import lib.datastructures.Pair;
import models.address.Address;
import models.assets.FungibleAsset;
import models.contract.Contract;
import models.contract.ContractInstance;
import models.contract.SingleUseSeal;
import models.dto.requests.Message;
import models.dto.requests.SignedMessage;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.requests.contract.function.FunctionCall;
import models.dto.requests.contract.function.PayToContract;
import models.dto.requests.event.EventTriggerSchedulingRequest;
import models.dto.responses.Response;
import models.dto.responses.SuccessDataResponse;
import shared.SharedMemory;
import storage.ContractInstancesStorage;
import storage.ContractsStorage;
import vm.dfa.DeterministicFiniteAutomata;
import vm.types.AssetType;
import vm.types.FloatType;
import vm.types.TraceChange;
import vm.types.Type;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class VirtualMachine extends Thread {
    private final RequestQueue queue;
    private final SharedMemory<Response> sharedMemory;
    private int offset = 0;
    private final ContractsStorage contractsStorage;
    private final ContractInstancesStorage contractInstancesStorage;

    public VirtualMachine(
            RequestQueue queue,
            SharedMemory<Response> sharedMemory,
            ContractsStorage contractsStorage,
            ContractInstancesStorage contractInstancesStorage
    ) {
        super(VirtualMachine.class.getSimpleName());
        this.queue = queue;
        this.sharedMemory = sharedMemory;
        this.contractsStorage = contractsStorage;
        this.contractInstancesStorage = contractInstancesStorage;
    }

    @Override
    public void run() {
        Pair<Thread, Pair<String, Object>> request;
        Pair<String, Object> packet;
        Thread thread;
        String whereToNotify;
        SignedMessage signedMessage = null;
        EventTriggerSchedulingRequest triggerRequest = null;

        while (true) {
            System.out.println("VirtualMachine: Ready to dequeue a value...");
            try {
                request = this.queue.dequeue();
                System.out.println("VirtualMachine: request => " + request);

                thread = request.getFirst();
                packet = request.getSecond();
                whereToNotify = packet.getFirst();

                if (packet.getSecond() instanceof SignedMessage) {
                    signedMessage = (SignedMessage) packet.getSecond();
                } else if (packet.getSecond() instanceof EventTriggerSchedulingRequest) {
                    triggerRequest = (EventTriggerSchedulingRequest) packet.getSecond();
                } else {
                    // Error
                }

                if (signedMessage != null) {
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
                        instance = new ContractInstance(agreementCall.getContractId(), stateMachine);
                        contractInstancesStorage.createContractInstance(instance);
                        System.out.println("main: contractInstanceId = " + instance.getInstanceId());

                        rawBytecode = this.loadFunction(agreementCall.getContractId(), "agreement");
                        System.out.println("main: loadFunction\n" + rawBytecode);

                        // TODO: Call SmartContractVirtualMachine in order to execute the request


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
                        //contractInstancesStorage.storeGlobalStorage(vm.getGlobalSpace(), instance);
                        System.out.println("main: Global store updated");
                    }

                    // contractInstancesStorage.close();

                    System.out.println("GLOBALSPACE");
                    System.out.println(instance.getGlobalVariables());
                    System.out.println(contractInstancesStorage.getContractInstance(instance.getInstanceId()).getGlobalVariables());


                    if (thread != null && whereToNotify != null) {
                        if (this.sharedMemory.containsKey(whereToNotify)) {
                            System.out.println("VirtualMachine: response from Storage " + this.sharedMemory.get(whereToNotify));

                            this.sharedMemory.set(
                                    whereToNotify,
                                    new SuccessDataResponse("ack from VirtualMachine")
                            );

                            System.out.println("VirtualMachine: Now I'll notify the thread " + thread.getName());
                            synchronized (thread) {
                                thread.notify();
                            }

                            System.out.println("VirtualMachine: Bye bye!");
                        } else {
                            System.out.println("VirtualMachine: Oh no! There is no reference in the shared space for this thread " + whereToNotify);
                        }
                    }
                } else if (triggerRequest != null) {

                }
            } catch (QueueUnderflowException error) {
                // throw new RuntimeException(e);
                try {
                    System.out.println("VirtualMachine: I'm waiting...");
                    synchronized (this) {
                        this.wait();
                    }
                } catch (InterruptedException ex) {
                    System.out.println("VirtualMachine: " + ex);
                    throw new RuntimeException(ex);
                }
            } catch (IOException | QueueOverflowException | InterruptedException | NoSuchAlgorithmException e) {
                System.out.println("VirtualMachine: " + e);
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String loadFunction(String contractId, String function) throws IOException {
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

    private HashMap<String, String> loadArguments(Message message) {
        HashMap<String, String> arguments = new HashMap<>();

        if (message instanceof AgreementCall) {
            AgreementCall agreementCall = (AgreementCall) message;
            arguments.putAll(agreementCall.getArguments());

            for (HashMap.Entry<String, Address> entry : agreementCall.getParties().entrySet()) {
                arguments.put(entry.getKey(), entry.getValue().getPublicKey());
            }
        } else {
            FunctionCall functionCall = (FunctionCall) message;
            arguments.putAll(functionCall.getArguments());
        }

        return arguments;
    }

    private HashMap<String, AssetType> loadAssetArguments(Message message) throws Exception {
        HashMap<String, AssetType> assetArguments = new HashMap<>();

        if (message instanceof FunctionCall) {
            FunctionCall functionCall = (FunctionCall) message;

            if (!functionCall.getAssetArguments().isEmpty()) {
                for (HashMap.Entry<String, PayToContract> entry : functionCall.getAssetArguments().entrySet()) {
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

    private String getNextStateFromFunction(String contractId, String function) throws IOException {
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

    private String loadBytecode(String rawBytecode, HashMap<String, String> arguments) {
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

    private String loadBytecode(String rawBytecode, HashMap<String, String> arguments, HashMap<String, AssetType> assetArguments) {
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
}

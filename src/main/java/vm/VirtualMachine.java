package vm;

import event.EventTrigger;
import event.EventTriggerHandler;
import exceptions.queue.QueueUnderflowException;
import lib.datastructures.Pair;
import models.address.Address;
import models.assets.Asset;
import models.contract.Contract;
import models.contract.ContractInstance;
import models.contract.Property;
import models.contract.SingleUseSeal;
import models.dto.requests.Message;
import models.dto.requests.SignedMessage;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.requests.contract.function.FunctionCall;
import models.dto.requests.contract.function.PayToContract;
import models.dto.requests.event.EventTriggerRequest;
import models.dto.requests.event.EventTriggerSchedulingRequest;
import models.dto.responses.Response;
import models.dto.responses.SuccessDataResponse;
import shared.SharedMemory;
import storage.*;
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
    private final EventTriggerHandler eventTriggerHandler;
    private final ContractsStorage contractsStorage;
    private final ContractInstancesStorage contractInstancesStorage;
    private final AssetsStorage assetsStorage;
    private final PropertiesStorage propertiesStorage;

    public VirtualMachine(
            RequestQueue queue,
            SharedMemory<Response> sharedMemory,
            EventTriggerHandler eventTriggerHandler,
            ContractsStorage contractsStorage,
            ContractInstancesStorage contractInstancesStorage,
            AssetsStorage assetsStorage,
            PropertiesStorage propertiesStorage
    ) {
        super(VirtualMachine.class.getSimpleName());
        this.queue = queue;
        this.sharedMemory = sharedMemory;
        this.eventTriggerHandler = eventTriggerHandler;
        this.contractsStorage = contractsStorage;
        this.contractInstancesStorage = contractInstancesStorage;
        this.assetsStorage = assetsStorage;
        this.propertiesStorage = propertiesStorage;
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
                                contract.getEndStates(),
                                contract.getTransitions()
                        );

                        // Create a new instance of the contract
                        instance = new ContractInstance(agreementCall.getContractId(), stateMachine);
                        contractInstancesStorage.createContractInstance(instance);
                        System.out.println("VirtualMachine: contractInstanceId = " + instance.getInstanceId());

                        rawBytecode = this.loadFunction(agreementCall.getContractId(), "agreement");
                        System.out.println("VirtualMachine: Function\n" + rawBytecode);
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
                        System.out.println("VirtualMachine: nextState: " + nextState);

                        rawBytecode = loadFunction(functionCall.getContractId(), functionCall.getFunction());
                        System.out.println("VirtualMachine: Function\n" + rawBytecode);
                    }

                    // Load arguments
                    HashMap<String, String> arguments = loadArguments(message);

                    // Load asset arguments
                    HashMap<String, AssetType> assetArguments = new HashMap<>();
                    HashMap<String, PropertyUpdateData> propertiesToUpdate = new HashMap<>();

                    if (message instanceof FunctionCall) {
                        // Properties validation
                        if (!validateProperties(address, (FunctionCall) message)) {
                            throw new Error("Not all the properties are valid");
                        }

                        // (Effectively) Load asset arguments
                        assetArguments = loadAssetArguments((FunctionCall) message);

                        propertiesToUpdate = getPropertiesToUpdate(address, (FunctionCall) message);
                    }

                    // Load the bytecode
                    String bytecode;
                    if (message instanceof AgreementCall) {
                        bytecode = loadBytecode(rawBytecode, arguments);
                        System.out.println("VirtualMachine: loadBytecode\n" + bytecode);
                    } else {
                        bytecode = loadBytecode(rawBytecode, arguments, assetArguments);
                        System.out.println("VirtualMachine: loadBytecode\n" + bytecode);
                    }

                    String[] instructions = bytecode.split("\n");

                    // Check if the state is correct
                    if (message instanceof FunctionCall && !instance.getStateMachine().isNextState(nextState, address)) {
                        // Error
                        System.out.println("VirtualMachine: Error in the state machine");
                        System.out.println("VirtualMachine: Current state => " + instance.getStateMachine().getCurrentState());
                        System.out.println("VirtualMachine: Next state => " + nextState);
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
                        System.out.println("VirtualMachine: Error while executing the function");
                        return;
                    }

                    // Go to the next state
                    instance.getStateMachine().nextState(nextState, address);
                    System.out.println("VirtualMachine: current state = " + instance.getStateMachine().getCurrentState());

                    // Set up trigger events
                    for (EventTriggerRequest eventTriggerRequest : vm.getEventTriggersToRequest()) {
                        EventTriggerSchedulingRequest eventTriggerSchedulingRequest =
                                new EventTriggerSchedulingRequest(
                                        eventTriggerRequest,
                                        instance.getContractId(),
                                        instance.getInstanceId()
                                );
                        EventTrigger eventTrigger = new EventTrigger(
                                eventTriggerSchedulingRequest,
                                eventTriggerHandler,
                                queue,
                                this
                        );
                        eventTriggerHandler.addTask(eventTrigger);
                        System.out.println("VirtualMachine: add trigger => " + eventTrigger);
                    }

                    // Update the properties/single-use seals
                    for (HashMap.Entry<String, PropertyUpdateData> entry : propertiesToUpdate.entrySet()) {
                        PropertyUpdateData data = entry.getValue();
                        System.out.println("VirtualMachine: address => " + entry.getKey());
                        propertiesStorage.makePropertySpent(
                                entry.getKey(),
                                data.getPropertyId(),
                                data.getContractInstanceId(),
                                data.getUnlockScript()
                        );
                    }

                    System.out.println("VirtualMachine: Updating the global store...");
                    if (vm.getGlobalSpace().isEmpty()) {
                        System.out.println("There is nothing to save in the global store");
                    } else {
                        contractInstancesStorage.storeGlobalStorage(vm.getGlobalSpace(), instance);
                        System.out.println("VirtualMachine: Global store updated");
                    }

                    HashMap<String, SingleUseSeal> singleUseSealsToSend = vm.getSingleUseSealsToCreate();
                    propertiesStorage.addFunds(singleUseSealsToSend);

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
                    System.out.println("VirtualMachine: just received a trigger request");
                    String contractId = triggerRequest.getContractId();
                    String contractInstanceId = triggerRequest.getContractInstanceId();
                    String obligationFunctionName = triggerRequest.getRequest().getObligationFunctionName();

                    ContractInstance instance = contractInstancesStorage.getContractInstance(contractInstanceId);
                    String nextState = getNextStateFromFunction(contractId, obligationFunctionName);

                    String rawBytecode = loadFunction(contractId, obligationFunctionName);
                    System.out.println("VirtualMachine: Function\n" + rawBytecode);

                    String bytecode = loadBytecode(rawBytecode, new HashMap<String, String>());
                    System.out.println("VirtualMachine: loadBytecode\n" + bytecode);

                    String[] instructions = bytecode.split("\n");

                    // Check if the state is correct
                    if (!instance.getStateMachine().isNextState(nextState, obligationFunctionName)) {
                        // Warning
                        System.out.println("VirtualMachine: Error in the state machine");
                        System.out.println("VirtualMachine: Current state => " + instance.getStateMachine().getCurrentState());
                        System.out.println("VirtualMachine: Next state => " + nextState);
                        System.out.println(obligationFunctionName);
                        // System.exit(-1);
                    } else {
                        // Set up the virtual machine
                        SmartContractVirtualMachine vm;

                        // Load global storage
                        HashMap<String, TraceChange> global = new HashMap<>();

                        for (HashMap.Entry<String, Type> entry : instance.getGlobalVariables().entrySet()) {
                            global.put(entry.getKey(), new TraceChange(entry.getValue()));
                        }

                        vm = new SmartContractVirtualMachine(instructions, offset, global);

                        // Execute the code
                        boolean result = vm.execute();

                        if (!result) {
                            System.out.println("VirtualMachine: Error while executing the function");
                            return;
                        }

                        // Go to the next state
                        instance.getStateMachine().nextState(nextState, obligationFunctionName);
                        System.out.println("VirtualMachine: current state = " + instance.getStateMachine().getCurrentState());

                        System.out.println("VirtualMachine: Updating the global store...");
                        if (vm.getGlobalSpace().isEmpty()) {
                            System.out.println("There is nothing to save in the global store");
                        } else {
                            contractInstancesStorage.storeGlobalStorage(vm.getGlobalSpace(), instance);
                            System.out.println("VirtualMachine: Global store updated");
                        }

                        HashMap<String, SingleUseSeal> singleUseSealsToSend = vm.getSingleUseSealsToCreate();
                        propertiesStorage.addFunds(singleUseSealsToSend);

                        System.out.println("GLOBALSPACE");
                        System.out.println(instance.getGlobalVariables());
                        System.out.println(contractInstancesStorage.getContractInstance(instance.getInstanceId()).getGlobalVariables());
                    }
                }

                // Reset variables
                signedMessage = null;
                triggerRequest = null;
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
            } catch (IOException | InterruptedException | NoSuchAlgorithmException e) {
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
        Contract contract = contractsStorage.getContract(contractId);
        String bytecode = contract.getBytecode();
        String[] instructions = bytecode.split("\n");

        System.out.println("loadFunction: Loading the function...");
        for (int i = 0; i < instructions.length && !isFunctionEnded; i++) {
            String[] instruction = instructions[i].trim().split(" ");

            if (isFunctionStarted) {
                bytecodeFunction += instructions[i].trim() + "\n";
            }

            if ((instruction[0].equals("fn") || instruction[0].equals("obligation")) && instruction[1].equals(function)) {
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

    private boolean validateProperties(Address address, FunctionCall functionCall) throws Exception {
        if (!functionCall.getAssetArguments().isEmpty()) {
            for (HashMap.Entry<String, PayToContract> entry : functionCall.getAssetArguments().entrySet()) {
                PayToContract payToContract = entry.getValue();
                Property property = payToContract.getProperty();

                Property propertyFromStorage = propertiesStorage.getFund(address.getAddress(), property.getId());
                if (propertyFromStorage == null) {
                    // TODO: Error: the property does not exist in the storage
                    System.out.println("validateProperties: the property does not exist in the storage");
                    return false;
                }

                if (!propertyFromStorage.getUnlockScript().equals("") && !propertyFromStorage.getContractInstanceId().equals("")) {
                    // TODO: Error: the property has been spent
                    System.out.println("validateProperties: the property has been spent");
                    return false;
                }

                SingleUseSeal singleUseSeal = property.getSingleUseSeal();

                // TODO: Check if the single-use seal exists
                // singleUseSeal.getId()

                Asset asset = assetsStorage.getAsset(singleUseSeal.getAssetId());

                if (asset == null) {
                    // TODO: Error
                    return false;
                }

                // Check if the decimals matches
                if (!(singleUseSeal.getAmount().getDecimals() == asset.getAsset().getDecimals())) {
                    // Error
                }

                // Check if the amount <= asset supply
                if (!(singleUseSeal.getAmount().getInteger() <= asset.getAsset().getSupply())) {
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
                    return false;
                }

                System.out.println("loadAssetArguments: Script validated");
            }
        }

        return true;
    }

    private HashMap<String, AssetType> loadAssetArguments(FunctionCall functionCall) throws Exception {
        HashMap<String, AssetType> assetArguments = new HashMap<>();

        if (!functionCall.getAssetArguments().isEmpty()) {
            for (HashMap.Entry<String, PayToContract> entry : functionCall.getAssetArguments().entrySet()) {
                PayToContract payToContract = entry.getValue();
                SingleUseSeal singleUseSeal = payToContract.getProperty().getSingleUseSeal();

                // TODO: Check if the single-use seal exists
                // singleUseSeal.getId()

                Asset asset = assetsStorage.getAsset(singleUseSeal.getAssetId());

                AssetType value = new AssetType(
                        asset.getId(),
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
    }

    private HashMap<String, PropertyUpdateData> getPropertiesToUpdate(Address address, FunctionCall functionCall) throws Exception {
        HashMap<String, PropertyUpdateData> propertiesToUpdate = new HashMap<>();

        if (!functionCall.getAssetArguments().isEmpty()) {
            for (HashMap.Entry<String, PayToContract> entry : functionCall.getAssetArguments().entrySet()) {
                PayToContract payToContract = entry.getValue();
                PropertyUpdateData data = new PropertyUpdateData(
                        payToContract.getProperty().getId(),
                        functionCall.getContractInstanceId(),
                        payToContract.getUnlockScript()
                );
                propertiesToUpdate.put(address.getAddress(), data);
            }
        }

        return propertiesToUpdate;
    }

    private String getNextStateFromFunction(String contractId, String function) throws IOException {
        String nextState = "";

        // Load all the bytecode
        Contract contract = contractsStorage.getContract(contractId);
        String bytecode = contract.getBytecode();
        String[] instructions = bytecode.split("\n");

        System.out.println("loadFunction: Loading the function...");
        for (String s : instructions) {
            String[] instruction = s.trim().split(" ");

            if (instruction[0].equals("fn") && instruction[1].equals(function)) {
                nextState = instruction[3];
                break;
            }

            if (instruction[0].equals("obligation") && instruction[1].equals(function)) {
                nextState = instruction[2];
                break;
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
                substitution += value.getValue().getInteger() + " " + value.getValue().getDecimals() + " " + value.getAssetId();
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

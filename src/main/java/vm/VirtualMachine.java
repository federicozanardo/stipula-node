package vm;

import exceptions.datastructures.queue.QueueUnderflowException;
import exceptions.models.dto.requests.contract.function.UnsupportedTypeException;
import exceptions.storage.AssetNotFoundException;
import exceptions.storage.ContractNotFoundException;
import exceptions.storage.OwnershipNotFoundException;
import exceptions.storage.OwnershipsNotFoundException;
import lib.datastructures.Pair;
import models.assets.Asset;
import models.contract.*;
import models.dto.requests.Message;
import models.dto.requests.SignedMessage;
import models.dto.requests.contract.FunctionArgument;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.requests.contract.function.FunctionCall;
import models.dto.requests.event.CreateEventRequest;
import models.dto.requests.event.EventSchedulingRequest;
import models.dto.responses.VirtualMachineResponse;
import models.party.Party;
import shared.SharedMemory;
import storage.AssetsStorage;
import storage.ContractInstancesStorage;
import storage.ContractsStorage;
import storage.OwnershipsStorage;
import vm.dfa.DeterministicFiniteAutomata;
import vm.dfa.states.DfaState;
import vm.event.EventScheduler;
import vm.event.EventTrigger;
import vm.types.AssetType;
import vm.types.RealType;
import vm.types.TraceChange;
import vm.types.Type;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VirtualMachine extends Thread {
    private final RequestQueue queue;
    private final SharedMemory<VirtualMachineResponse> sharedMemory;
    private int offset = 0;
    private final EventScheduler eventScheduler;
    private final ContractsStorage contractsStorage;
    private final ContractInstancesStorage contractInstancesStorage;
    private final AssetsStorage assetsStorage;
    private final OwnershipsStorage ownershipsStorage;

    public VirtualMachine(
            RequestQueue queue,
            SharedMemory<VirtualMachineResponse> sharedMemory,
            EventScheduler eventScheduler,
            ContractsStorage contractsStorage,
            ContractInstancesStorage contractInstancesStorage,
            AssetsStorage assetsStorage,
            OwnershipsStorage ownershipsStorage
    ) {
        super(VirtualMachine.class.getSimpleName());
        this.queue = queue;
        this.sharedMemory = sharedMemory;
        this.eventScheduler = eventScheduler;
        this.contractsStorage = contractsStorage;
        this.contractInstancesStorage = contractInstancesStorage;
        this.assetsStorage = assetsStorage;
        this.ownershipsStorage = ownershipsStorage;
    }

    @Override
    public void run() {
        Pair<Thread, Object> request;
        Thread thread;
        SignedMessage signedMessage;
        EventSchedulingRequest triggerRequest;
        boolean errorInStateMachine;

        while (true) {
            // Reset variables
            signedMessage = null;
            triggerRequest = null;
            errorInStateMachine = false;
            HashMap<String, TraceChange> globalSpace = new HashMap<>();

            System.out.println("VirtualMachine: Ready to dequeue a value...");
            try {
                // Dequeue a request from the request queue
                request = this.queue.dequeue();
                System.out.println("VirtualMachine: Request received => " + request);

                // Get the thread to communicate with the client socket
                thread = request.getFirst();

                if (request.getSecond() instanceof SignedMessage) {
                    signedMessage = (SignedMessage) request.getSecond();
                } else if (request.getSecond() instanceof EventSchedulingRequest) {
                    triggerRequest = (EventSchedulingRequest) request.getSecond();
                } else {
                    System.out.println("VirtualMachine: Request not valid");
                    sharedMemory.notifyThread(thread, new VirtualMachineResponse(400));
                }

                if (signedMessage != null) {
                    Message message = signedMessage.getMessage();

                    if (!(message instanceof AgreementCall) && !(message instanceof FunctionCall)) {
                        System.out.println("VirtualMachine: Message not valid");
                        sharedMemory.notifyThread(thread, new VirtualMachineResponse(401));
                    } else {
                        Contract contract;
                        ContractInstance contractInstance;
                        DfaState nextState;
                        Party party = null;
                        String functionName = "";
                        String contractId;
                        String contractInstanceId;

                        String partyName = "";
                        ArrayList<String> argumentsTypes = new ArrayList<>();
                        String rawBytecode = "";

                        // Load the function
                        if (message instanceof AgreementCall) {
                            AgreementCall agreementCall = (AgreementCall) message;
                            contractId = agreementCall.getContractId();
                            HashMap<String, Party> parties = agreementCall.getParties();
                            ArrayList<FunctionArgument> arguments = agreementCall.getArguments();

                            // Get the contract
                            contract = contractsStorage.getContract(contractId);

                            // Create an instance of a DFA
                            DeterministicFiniteAutomata deterministicFiniteAutomata = new DeterministicFiniteAutomata(
                                    contract.getInitialState(),
                                    contract.getFinalStates(),
                                    contract.getTransitions()
                            );

                            // Create a new instance of the contract
                            contractInstance = new ContractInstance(contractId, deterministicFiniteAutomata, parties);

                            // Save the new contract instance in the storage
                            try {
                                contractInstanceId = contractInstancesStorage.saveContractInstance(contractInstance);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            System.out.println("VirtualMachine: contractInstanceId = " + contractInstanceId);

                            // Get the party names
                            ArrayList<String> partiesNames = new ArrayList<>();
                            for (Map.Entry<String, Party> entry : parties.entrySet()) {
                                partiesNames.add(entry.getKey());
                            }

                            // Get the destination state
                            DfaState initialState = contract.getInitialState();

                            // Get the types of the arguments
                            for (FunctionArgument argument : arguments) {
                                argumentsTypes.add(argument.getType());
                            }

                            rawBytecode = this.loadAgreementFunction(contractId, partiesNames, initialState, argumentsTypes);
                            System.out.println("VirtualMachine: Function\n" + rawBytecode);
                        } else {
                            FunctionCall functionCall = (FunctionCall) message;
                            functionName = functionCall.getFunctionName();
                            contractInstanceId = functionCall.getContractInstanceId();
                            ArrayList<FunctionArgument> arguments = functionCall.getArguments();

                            // Get the instance of the contract
                            contractInstance = contractInstancesStorage.getContractInstance(contractInstanceId);

                            // Get the contract id
                            contractId = contractInstance.getContractId();

                            // Get the types of the arguments
                            for (FunctionArgument argument : arguments) {
                                argumentsTypes.add(argument.getType());
                            }

                            // Get the address of the party
                            Map.Entry<String, String> first = signedMessage.getSignatures().entrySet().iterator().next();
                            String publicKeyParty = first.getKey();
                            party = new Party(publicKeyParty);

                            // Get the name of the party from the address
                            for (HashMap.Entry<String, Party> entry : contractInstance.getParties().entrySet()) {
                                if (entry.getValue().getAddress().equals(party.getAddress())) {
                                    partyName = entry.getKey();
                                }
                            }

                            if (partyName != null) {
                                // Get the current state and the 'candidate' next state
                                DfaState currentState = contractInstance.getStateMachine().getCurrentState();
                                nextState = getNextStateFromCommonFunction(contractId, currentState, partyName, functionName, argumentsTypes);

                                // Check if the next state is correct
                                if (!contractInstance.getStateMachine().isNextState(partyName, functionName, nextState, argumentsTypes)) {
                                    errorInStateMachine = true;
                                    System.out.println("VirtualMachine: This function cannot be called in the current state");
                                    System.out.println("VirtualMachine: Party name => " + partyName);
                                    System.out.println("VirtualMachine: Function name => " + functionName);
                                    System.out.println("VirtualMachine: Argument types => " + argumentsTypes);
                                    System.out.println("VirtualMachine: Current state => " + currentState);
                                    System.out.println("VirtualMachine: Next state => " + nextState);
                                    System.exit(-1);
                                    sharedMemory.notifyThread(thread, new VirtualMachineResponse(402));
                                } else {
                                    System.out.println("VirtualMachine: nextState: " + nextState);
                                }

                                rawBytecode = loadCommonFunction(contractId, currentState, partyName, functionName, nextState, argumentsTypes);
                                System.out.println("VirtualMachine: Function\n" + rawBytecode);
                            } else {
                                sharedMemory.notifyThread(thread, new VirtualMachineResponse(403));
                            }
                        }

                        /*if (errorInStateMachine) {

                        }*/

                        ArrayList<PayToContract> ownershipsToUpdate = new ArrayList<>();

                        if (message instanceof FunctionCall) {
                            // Ownerships validation
                            FunctionCall functionCall = (FunctionCall) message;
                            ownershipsToUpdate = validateOwnerships(party, functionCall);
                        }

                        // Load arguments
                        ArrayList<FunctionArgument> arguments = loadArguments(message);

                        // Load the bytecode
                        String bytecode;
                        bytecode = loadBytecode(rawBytecode, arguments);
                        System.out.println("VirtualMachine: bytecode => \n" + bytecode);
                        String[] instructions = bytecode.split("\n");

                        // Set up the virtual machine
                        LegalContractVirtualMachine vm;

                        if (message instanceof AgreementCall) {
                            vm = new LegalContractVirtualMachine(instructions, offset);
                        } else {
                            // Load global space
                            for (HashMap.Entry<String, Type> entry : contractInstance.getGlobalSpace().entrySet()) {
                                globalSpace.put(entry.getKey(), new TraceChange(entry.getValue()));
                            }

                            vm = new LegalContractVirtualMachine(instructions, offset, globalSpace);
                        }

                        // Execute the code
                        boolean result = vm.execute();

                        if (!result) {
                            System.out.println("VirtualMachine: Error while executing the function");
                            return;
                        }

                        if (message instanceof FunctionCall) {
                            // Go to the next state
                            contractInstancesStorage.storeStateMachine(contractInstanceId, partyName, functionName, argumentsTypes);
                        }

                        // Set up trigger events
                        for (CreateEventRequest createEventRequest : vm.getCreateEventRequests()) {
                            EventSchedulingRequest eventSchedulingRequest =
                                    new EventSchedulingRequest(
                                            createEventRequest,
                                            contractId,
                                            contractInstanceId
                                    );
                            EventTrigger eventTrigger = new EventTrigger(
                                    eventSchedulingRequest,
                                    eventScheduler,
                                    queue,
                                    this
                            );
                            eventScheduler.addTask(eventTrigger);
                        }

                        // Update the ownerships
                        for (PayToContract ownershipToUpdate : ownershipsToUpdate) {
                            ownershipsStorage.makeOwnershipSpent(
                                    ownershipToUpdate.getAddress(),
                                    ownershipToUpdate.getOwnershipId(),
                                    contractInstanceId,
                                    ownershipToUpdate.getUnlockScript()
                            );
                        }

                        // Update the global space of the contract instance
                        if (vm.getGlobalSpace().isEmpty()) {
                            System.out.println("There is nothing to save in the global store");
                        } else {
                            System.out.println("VirtualMachine: Updating the global store...");
                            contractInstancesStorage.storeGlobalSpace(contractInstanceId, vm.getGlobalSpace());
                            System.out.println("VirtualMachine: Global store updated");
                        }

                        // Store the new single-use seals produced by the contract execution
                        HashMap<String, SingleUseSeal> singleUseSealsToSend = vm.getSingleUseSealsToCreate();
                        ownershipsStorage.addFunds(singleUseSealsToSend);

                        sharedMemory.notifyThread(thread, new VirtualMachineResponse(200));
                    }
                } else if (triggerRequest != null) {
                    System.out.println("VirtualMachine: Just received a trigger request");
                    String contractId = triggerRequest.getContractId();
                    String contractInstanceId = triggerRequest.getContractInstanceId();
                    String obligationFunctionName = triggerRequest.getRequest().getObligationFunctionName();

                    // Get the instance of the contract from the storage
                    ContractInstance contractInstance = contractInstancesStorage.getContractInstance(contractInstanceId);

                    // Get the current state and the 'candidate' next state
                    DfaState currentState = contractInstance.getStateMachine().getCurrentState();
                    DfaState nextState = getNextStateFromObligationFunction(contractId, currentState, obligationFunctionName);

                    // Check if the state is correct
                    if (!contractInstance.getStateMachine().isNextState(obligationFunctionName, nextState)) {
                        System.out.println("VirtualMachine: This function cannot be called in the current state");
                        System.out.println("VirtualMachine: Obligation function name => " + obligationFunctionName);
                        System.out.println("VirtualMachine: Current state => " + currentState);
                        System.out.println("VirtualMachine: Next state => " + nextState);
                    } else {
                        String rawBytecode = loadObligationFunction(contractId, currentState, obligationFunctionName, nextState);
                        System.out.println("VirtualMachine: Function\n" + rawBytecode);

                        String bytecode = loadBytecode(rawBytecode, new ArrayList<>()); // FIXME
                        System.out.println("VirtualMachine: loadBytecode\n" + bytecode);

                        String[] instructions = bytecode.split("\n");

                        // Set up the virtual machine
                        LegalContractVirtualMachine vm;

                        // Load global space
                        for (HashMap.Entry<String, Type> entry : contractInstance.getGlobalSpace().entrySet()) {
                            globalSpace.put(entry.getKey(), new TraceChange(entry.getValue()));
                        }

                        vm = new LegalContractVirtualMachine(instructions, offset, globalSpace);

                        // Execute the code
                        boolean result = vm.execute();

                        if (!result) {
                            System.out.println("VirtualMachine: Error while executing the function");
                            return;
                        }

                        // Go to the next state
                        contractInstancesStorage.storeStateMachine(contractInstanceId, obligationFunctionName);

                        // Update the global space of the contract instance
                        if (vm.getGlobalSpace().isEmpty()) {
                            System.out.println("There is nothing to save in the global store");
                        } else {
                            System.out.println("VirtualMachine: Updating the global store...");
                            contractInstancesStorage.storeGlobalSpace(contractInstanceId, vm.getGlobalSpace());
                            System.out.println("VirtualMachine: Global store updated");
                        }

                        // Store the new single-use seals produced by the contract execution
                        HashMap<String, SingleUseSeal> singleUseSealsToSend = vm.getSingleUseSealsToCreate();
                        ownershipsStorage.addFunds(singleUseSealsToSend);
                    }
                }
            } catch (QueueUnderflowException exception) {
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

    /**
     * This method allows to load the agreement function from a contract.
     *
     * @param contractId:       id of the contract from which to load the bytecode.
     * @param parties:          party names of the agreement function.
     * @param destinationState: destination state of the agreement function.
     * @param argumentTypes:    list of argument types for the agreement function.
     * @return return the bytecode code of the agreement function.
     * @throws IOException:               throws when an error occur while opening or closing the connection with the storage.
     * @throws ContractNotFoundException: throws when the contract id is not referred to any contract saved in the storage.
     */
    private String loadAgreementFunction(
            String contractId,
            ArrayList<String> parties,
            DfaState destinationState,
            ArrayList<String> argumentTypes
    ) throws IOException, ContractNotFoundException {
        // '*' means optional
        // fn agreement <parties>       <destination_state> <type_args*>

        if (parties == null) {
            throw new NullPointerException("Impossible to load the agreement function without the parties of the contract");
        }

        // TODO: check if it is an error to accept more than 3 parties
        if (parties.size() < 2 /*|| parties.size() > 3*/) {
            throw new RuntimeException("Number of parties is not legit"); // FIXME: create a custom error and write a better error message
        }

        String bytecodeFunction = "";
        boolean isFunctionStarted = false;
        boolean isFunctionEnded = false;
        boolean areTypesToInfer = false;
        ArrayList<Pair<Integer, String>> indexesOfArgumentsToInfer = new ArrayList<>();

        // Load all the bytecode of the contract
        Contract contract = contractsStorage.getContract(contractId);
        String bytecode = contract.getBytecode();
        String[] instructions = bytecode.split("\n");

        System.out.println("loadAgreementFunction: Loading the function...");
        for (int i = 0; i < instructions.length && !isFunctionEnded; i++) {
            String[] instruction = instructions[i].trim().split(" ");

            if (isFunctionStarted) {
                bytecodeFunction += instructions[i].trim() + "\n";
            }

            if (instruction[0].equals("fn") && instruction[1].equals("agreement")) {
                // Check the parties
                boolean arePartiesCorrect = true;
                String[] partiesFromFunction = instruction[2].split(",");

                if (partiesFromFunction.length == parties.size()) {
                    for (int j = 0; j < partiesFromFunction.length; j++) {
                        String partyFromFunction = partiesFromFunction[j];

                        if (!parties.contains(partyFromFunction)) {
                            arePartiesCorrect = false;
                            break;
                        }
                    }
                }

                if (arePartiesCorrect) {
                    // Check the destination state
                    if (instruction[3].equals(destinationState.getName())) {
                        if (instruction.length == 5 && argumentTypes != null) {
                            // Check the arguments types
                            boolean areArgumentsTypesCorrect = true;
                            String[] typeArgsFromFunction = instruction[4].split(",");

                            if (typeArgsFromFunction.length == argumentTypes.size()) {
                                // Check the arguments types
                                for (int j = 0; j < typeArgsFromFunction.length; j++) {
                                    String typeArgFromFunction = typeArgsFromFunction[j];

                                    if (typeArgFromFunction.equals("*")) {
                                        areTypesToInfer = true;
                                        indexesOfArgumentsToInfer.add(new Pair<>(j, argumentTypes.get(j)));
                                    } else {
                                        if (!typeArgFromFunction.equals(argumentTypes.get(j))) {
                                            areArgumentsTypesCorrect = false;
                                        }
                                    }
                                }

                                if (areArgumentsTypesCorrect) {
                                    isFunctionStarted = true;
                                    offset = i + 1;
                                }
                            }
                        }
                    }
                }
            }

            if (instruction[0].equals("HALT") && isFunctionStarted) {
                isFunctionEnded = true;
            }
        }
        System.out.println("loadAgreementFunction: Function loaded");

        if (areTypesToInfer) {
            for (Pair<Integer, String> index : indexesOfArgumentsToInfer) {
                bytecodeFunction = setTypeForDynamicVariable(bytecodeFunction, index.getFirst(), index.getSecond());
            }
        }

        return bytecodeFunction;
    }

    /**
     * This method allows to load a common function from a contract.
     *
     * @param contractId:       id of the contract from which to load the bytecode.
     * @param sourceState:      source state of the common function.
     * @param party:            party name of the common function.
     * @param functionName:     name of the function to load.
     * @param destinationState: destination state of the common function.
     * @param argumentTypes:    list of argument types for the common function.
     * @return return the bytecode code of the common function.
     * @throws IOException:               throws when an error occur while opening or closing the connection with the storage.
     * @throws ContractNotFoundException: throws when the contract id is not referred to any contract saved in the storage.
     */
    private String loadCommonFunction(
            String contractId,
            DfaState sourceState,
            String party,
            String functionName,
            DfaState destinationState,
            ArrayList<String> argumentTypes
    ) throws IOException, ContractNotFoundException {
        // '*' means optional
        // fn <source_state> <party> <function_name> <destination_state> <type_args*>

        String bytecodeFunction = "";
        boolean isFunctionStarted = false;
        boolean isFunctionEnded = false;
        boolean areTypesToInfer = false;
        ArrayList<Pair<Integer, String>> indexesOfArgumentsToInfer = new ArrayList<>();

        // Load all the bytecode
        Contract contract = contractsStorage.getContract(contractId);
        String bytecode = contract.getBytecode();
        String[] instructions = bytecode.split("\n");

        System.out.println("loadCommonFunction: Loading the function...");
        for (int i = 0; i < instructions.length && !isFunctionEnded; i++) {
            String[] instruction = instructions[i].trim().split(" ");

            if (isFunctionStarted) {
                bytecodeFunction += instructions[i].trim() + "\n";
            }

            if (instruction[0].equals("fn") &&
                    instruction[1].equals(sourceState.getName()) &&
                    instruction[2].equals(party) &&
                    instruction[3].equals(functionName) &&
                    instruction[4].equals(destinationState.getName())
            ) {
                if (instruction.length == 6 && argumentTypes != null && argumentTypes.size() > 0) {
                    // Check the arguments types
                    boolean areArgumentTypesCorrect = true;
                    String[] argumentTypesFromFunction = instruction[5].split(",");

                    if (argumentTypesFromFunction.length == argumentTypes.size()) {
                        // Check the arguments types
                        for (int j = 0; j < argumentTypesFromFunction.length; j++) {
                            String argumentTypeFromFunction = argumentTypesFromFunction[j];

                            if (argumentTypeFromFunction.equals("*")) {
                                areTypesToInfer = true;
                                indexesOfArgumentsToInfer.add(new Pair<>(j, argumentTypes.get(j)));
                            } else {
                                if (!argumentTypeFromFunction.equals(argumentTypes.get(j))) {
                                    areArgumentTypesCorrect = false;
                                }
                            }
                        }

                        if (areArgumentTypesCorrect) {
                            isFunctionStarted = true;
                            offset = i + 1;
                        }
                    }
                } else {
                    isFunctionStarted = true;
                    offset = i + 1;
                }
            }

            if (instruction[0].equals("HALT") && isFunctionStarted) {
                isFunctionEnded = true;
            }
        }
        System.out.println("loadCommonFunction: Function loaded");

        if (areTypesToInfer) {
            for (Pair<Integer, String> index : indexesOfArgumentsToInfer) {
                bytecodeFunction = setTypeForDynamicVariable(bytecodeFunction, index.getFirst(), index.getSecond());
            }
        }

        return bytecodeFunction;
    }

    /**
     * Set the type for untyped argument.
     *
     * @param bytecode: represents the bytecode of a function.
     * @param index:    specify which argument is untyped.
     * @param type:     new type for the untyped argument.
     * @return return the bytecode with all the arguments typed.
     */
    private String setTypeForDynamicVariable(String bytecode, int index, String type) {
        boolean isArgumentsDeclarationStarted = false;
        int i = 0;
        int k = 0;
        String[] instructions = bytecode.split("\n");
        String newBytecode = "";

        System.out.println("setTypeForDynamicVariable: Loading the function...");

        while (i < instructions.length) {
            String[] instruction = instructions[i].trim().split(" ");

            if (instruction[0].equals("start:")) {
                isArgumentsDeclarationStarted = false;
            }

            if (isArgumentsDeclarationStarted) {
                String[] secondInstruction = instructions[i + 1].trim().split(" ");
                String[] thirdInstruction = instructions[i + 2].trim().split(" ");

                if (instruction[0].equals("PUSH") && secondInstruction[0].equals("AINST") && thirdInstruction[0].equals("ASTORE")) {
                    if (k == index) {
                        newBytecode += "PUSH " + type + " " + instruction[2] + "\n";
                        newBytecode += "AINST " + type + " " + secondInstruction[2] + "\n";
                        newBytecode += instructions[i + 2] + "\n";

                        i += 3;
                    } else {
                        k++;
                    }
                }
            } else {
                newBytecode += instructions[i] + "\n";
                i++;
            }

            if (instruction[0].equals("args:")) {
                isArgumentsDeclarationStarted = true;
            }
        }
        return newBytecode;
    }

    /**
     * This method allows to load an obligation function from a contract.
     *
     * @param contractId:             id of the contract from which to load the bytecode.
     * @param sourceState:            source state of the common function.
     * @param obligationFunctionName: name of the obligation function to load.
     * @param destinationState:       destination state of the common function.
     * @return return the bytecode code of the obligation function.
     * @throws IOException:               throws when an error occur while opening or closing the connection with the storage.
     * @throws ContractNotFoundException: throws when the contract id is not referred to any contract saved in the storage.
     */
    private String loadObligationFunction(
            String contractId,
            DfaState sourceState,
            String obligationFunctionName,
            DfaState destinationState
    ) throws IOException, ContractNotFoundException {
        // obligation <source_state> <obligation_function_name> <destination_state>

        String bytecodeFunction = "";
        boolean isFunctionStarted = false;
        boolean isFunctionEnded = false;

        // Load all the bytecode
        Contract contract = contractsStorage.getContract(contractId);
        String bytecode = contract.getBytecode();
        String[] instructions = bytecode.split("\n");

        System.out.println("loadObligationFunction: Loading the obligation function...");
        for (int i = 0; i < instructions.length && !isFunctionEnded; i++) {
            String[] instruction = instructions[i].trim().split(" ");

            if (isFunctionStarted) {
                bytecodeFunction += instructions[i].trim() + "\n";
            }

            if (instruction[0].equals("obligation") &&
                    instruction[1].equals(sourceState.getName()) &&
                    instruction[2].equals(obligationFunctionName) &&
                    instruction[3].equals(destinationState.getName())
            ) {
                isFunctionStarted = true;
                offset = i + 1;
            }

            if (instruction[0].equals("HALT") && isFunctionStarted) {
                isFunctionEnded = true;
            }
        }
        System.out.println("loadObligationFunction: Obligation function loaded");

        return bytecodeFunction;
    }

    /**
     * From the message sent by the party, parse the arguments for the function.
     *
     * @param message:                      message sent by the party that contains the values for the arguments.
     * @return                              return the list of argument parsed from the message sent by the party.
     * @throws IOException:                 throws when an error occur while opening or closing the connection with the storage.
     * @throws AssetNotFoundException:      throws when the asset id is not referred to any asset saved in the storage.
     * @throws OwnershipsNotFoundException: throws when there are no funds associated to the given address.
     * @throws OwnershipNotFoundException:  throws when the ownership id is not referred to the given address or to any ownership saved in the storage.
     * @throws UnsupportedTypeException:    throws when there is a type not supported by {@link FunctionArgument}.
     */
    private ArrayList<FunctionArgument> loadArguments(Message message)
            throws AssetNotFoundException, IOException, OwnershipNotFoundException,
            OwnershipsNotFoundException, UnsupportedTypeException {
        ArrayList<FunctionArgument> arguments = new ArrayList<>();

        if (message instanceof AgreementCall) {
            AgreementCall agreementCall = (AgreementCall) message;
            ArrayList<FunctionArgument> agreementCallArguments = agreementCall.getArguments();
            HashMap<String, Party> parties = agreementCall.getParties();

            for (FunctionArgument functionArgument : agreementCallArguments) {
                if (functionArgument.getType().equals("asset")) {
                    throw new RuntimeException("Assets cannot be accepted by the agreement function as arguments");
                } else if (functionArgument.getType().equals("party")) {
                    throw new RuntimeException("Parties cannot be accepted by the agreement function as arguments");
                } else {
                    arguments.add(functionArgument);
                }
            }

            for (HashMap.Entry<String, Party> entry : parties.entrySet()) {
                arguments.add(new FunctionArgument("party", entry.getKey(), entry.getValue().getPublicKey()));
            }
        } else {
            FunctionCall functionCall = (FunctionCall) message;
            ArrayList<FunctionArgument> functionCallArguments = functionCall.getArguments();

            for (FunctionArgument functionArgument : functionCallArguments) {
                if (functionArgument.getType().equals("asset") && (functionArgument.getValue() instanceof PayToContract)) {
                    String type = functionArgument.getType();
                    String variableName = functionArgument.getVariableName();
                    PayToContract payToContract = (PayToContract) functionArgument.getValue();
                    String ownershipId = payToContract.getOwnershipId();
                    String address = payToContract.getAddress();

                    // Try to get the ownership from the storage
                    Ownership ownershipFromStorage = ownershipsStorage.getFund(address, ownershipId);
                    if (ownershipFromStorage == null) {
                        // TODO: Error: the ownership does not exist in the storage
                        System.out.println("loadArguments: the ownership does not exist in the storage");
                        throw new RuntimeException();
                    }

                    if (!ownershipFromStorage.getUnlockScript().equals("") && !ownershipFromStorage.getContractInstanceId().equals("")) {
                        // TODO: Error: the ownership has been spent
                        System.out.println("loadArguments: the ownership has been spent");
                        throw new RuntimeException();
                    }

                    // Get the single-use seal
                    SingleUseSeal singleUseSeal = ownershipFromStorage.getSingleUseSeal();
                    String assetId = singleUseSeal.getAssetId();

                    // Get the asset
                    Asset asset = assetsStorage.getAsset(assetId);

                    AssetType value = new AssetType(
                            asset.getId(),
                            new RealType(
                                    singleUseSeal.getAmount().getInteger(),
                                    singleUseSeal.getAmount().getDecimals()
                            )
                    );

                    String argumentValue = value.getValue().getInteger() + " " + value.getValue().getDecimals() + " " + value.getAssetId();
                    arguments.add(new FunctionArgument(type, variableName, argumentValue));
                } else if (functionArgument.getType().equals("party")) {
                    throw new RuntimeException("Parties cannot be accepted by the agreement function as arguments");
                } else {
                    arguments.add(functionArgument);
                }
            }
        }
        return arguments;
    }

    /**
     * Given the bytecode of a function and the arguments, return the bytecode function with argument variables instantiated.
     *
     * @param rawBytecode:  bytecode of the function, loaded from the contract.
     * @param arguments:    list of argument values to instantiate in the bytecode function.
     * @return              return the bytecode function with argument variables instantiated.
     */
    private String loadBytecode(String rawBytecode, ArrayList<FunctionArgument> arguments) {
        String bytecode = "";
        String substitution = "";
        String[] instructions = rawBytecode.split("\n");
        boolean isPushSet = false;

        System.out.println("loadBytecode: Loading the bytecode...");
        for (int i = 0; i < instructions.length; i++) {

            String[] instruction = instructions[i].trim().split(" ");

            // Check if the last element of the instruction starts with ':'
            if (instruction[instruction.length - 1].charAt(0) == ':') {
                int j = 0;
                boolean wasArgumentFound = false;

                while (j < arguments.size() && !wasArgumentFound) {
                    FunctionArgument argument = arguments.get(j);

                    if (argument.getVariableName().equals(instruction[instruction.length - 1].substring(1))) {
                        wasArgumentFound = true;

                        if (argument.getType().equals("asset")) {
                            if (isPushSet) {
                                for (int k = 0; k < instruction.length - 1; k++) {
                                    substitution += instruction[k] + " ";
                                }

                                String[] argumentValue = ((String) argument.getValue()).split(" ");
                                String last = argumentValue[argumentValue.length - 1];
                                String secondLast = argumentValue[argumentValue.length - 2];
                                substitution += instruction[instruction.length - 1].substring(1) + " " + secondLast + " " + last;

                                bytecode += substitution + "\n";
                                substitution = "";

                                isPushSet = false;
                            } else {
                                for (int k = 0; k < instruction.length - 1; k++) {
                                    substitution += instruction[k] + " ";
                                }
                                substitution += argument.getValue();

                                bytecode += substitution + "\n";
                                substitution = "";

                                // Check next instruction
                                String[] nextInstruction = instructions[i + 1].trim().split(" ");
                                if (nextInstruction[0].equals("INST") || nextInstruction[0].equals("AINST") || nextInstruction[0].equals("GINST")) {
                                    isPushSet = true;
                                }
                            }
                        } else if (argument.getType().equals("real")) {
                            if (isPushSet) {
                                for (int k = 0; k < instruction.length - 1; k++) {
                                    substitution += instruction[k] + " ";
                                }

                                String[] argumentValue = ((String) argument.getValue()).split(" ");
                                String last = argumentValue[argumentValue.length - 1];
                                substitution += instruction[instruction.length - 1].substring(1) + " " + last;

                                bytecode += substitution + "\n";
                                substitution = "";

                                isPushSet = false;
                            } else {
                                for (int k = 0; k < instruction.length - 1; k++) {
                                    substitution += instruction[k] + " ";
                                }
                                substitution += argument.getValue();

                                bytecode += substitution + "\n";
                                substitution = "";

                                // Check next instruction
                                String[] nextInstruction = instructions[i + 1].trim().split(" ");
                                if (nextInstruction[0].equals("INST") || nextInstruction[0].equals("AINST") || nextInstruction[0].equals("GINST")) {
                                    isPushSet = true;
                                }
                            }
                        } else {
                            if (instruction[0].equals("AINST")) {
                                for (int k = 0; k < instruction.length; k++) {
                                    substitution += instruction[k] + " ";
                                }
                                substitution = substitution.replace(":", "");

                                bytecode += substitution + "\n";
                                substitution = "";
                            } else {
                                for (int k = 0; k < instruction.length - 1; k++) {
                                    substitution += instruction[k] + " ";
                                }
                                substitution += argument.getValue();

                                bytecode += substitution + "\n";
                                substitution = "";
                            }
                        }
                    } else {
                        j++;
                    }
                }
            } else {
                bytecode += instructions[i].trim() + "\n";
            }
        }
        System.out.println("loadBytecode: Bytecode loaded\n");
        return bytecode;
    }

    /**
     * This method allows to validate the ownerships.
     *
     * @param party:                        the party that sent the payment.
     * @param functionCall:                 message sent by the party.
     * @return                              return the list of payments done by the party to the contract instance.
     * @throws IOException:                 throws when an error occur while opening or closing the connection with the storage.
     * @throws AssetNotFoundException:      throws when the asset id is not referred to any asset saved in the storage.
     * @throws OwnershipsNotFoundException: throws when there are no funds associated to the given address.
     * @throws OwnershipNotFoundException:  throws when the ownership id is not referred to the given address or to any ownership saved in the storage.
     */
    private ArrayList<PayToContract> validateOwnerships(Party party, FunctionCall functionCall)
            throws AssetNotFoundException, IOException, OwnershipNotFoundException, OwnershipsNotFoundException {
        ArrayList<PayToContract> ownershipsToUpdate = new ArrayList<>();
        ArrayList<FunctionArgument> arguments = functionCall.getArguments();

        if (!arguments.isEmpty()) {
            for (FunctionArgument argument : arguments) {

                if (argument.getType().equals("asset")) {
                    PayToContract payToContract = (PayToContract) argument.getValue();
                    String ownershipId = payToContract.getOwnershipId();
                    String unlockScript = payToContract.getUnlockScript();

                    // Try to get the ownership from the storage
                    Ownership ownershipFromStorage = ownershipsStorage.getFund(party.getAddress(), ownershipId);
                    if (ownershipFromStorage == null) {
                        // TODO: Error: the ownership does not exist in the storage
                        System.out.println("validateOwnerships: the ownership does not exist in the storage");
                        throw new RuntimeException();
                    }

                    if (!ownershipFromStorage.getUnlockScript().equals("") && !ownershipFromStorage.getContractInstanceId().equals("")) {
                        // TODO: Error: the ownership has been spent
                        System.out.println("validateOwnerships: the ownership has been spent");
                        throw new RuntimeException();
                    }

                    // Get the single-use seal
                    SingleUseSeal singleUseSeal = ownershipFromStorage.getSingleUseSeal();
                    String assetId = singleUseSeal.getAssetId();
                    String lockScript = singleUseSeal.getLockScript();

                    // Get the asset from the single-use seal
                    Asset asset = assetsStorage.getAsset(assetId);

                    if (asset == null) {
                        // TODO: Error
                        throw new RuntimeException();
                    }

                    // Check if the decimals matches
                    if (!(singleUseSeal.getAmount().getDecimals() == asset.getAsset().getDecimals())) {
                        // TODO: Error
                        throw new RuntimeException();
                    }

                    // Check if the amount > 0
                    BigDecimal zeroValue = new BigDecimal(0);
                    if (singleUseSeal.getAmount().getValue().compareTo(zeroValue) <= 0) {
                        // TODO: Error
                        throw new RuntimeException();
                    }

                    // Check if the amount <= asset supply
                    BigDecimal supply = new BigDecimal(asset.getAsset().getSupply());
                    if (singleUseSeal.getAmount().getValue().compareTo(supply) > 0) {
                        // TODO: Error
                        throw new RuntimeException();
                    }

                    // Check if it is possible to unlock the script
                    String script = unlockScript + lockScript;
                    System.out.println("validateOwnerships: Script to validate\n" + script);
                    String[] instructions = script.split("\n");

                    System.out.println("validateOwnerships: Start validating the script...");
                    ScriptVirtualMachine vm = new ScriptVirtualMachine(instructions, ownershipId);

                    // Execute the code
                    boolean result = vm.execute();

                    if (!result) {
                        System.out.println("validateOwnerships: Error while executing the function");
                        throw new RuntimeException();
                    }

                    ownershipsToUpdate.add((PayToContract) argument.getValue());
                    System.out.println("validateOwnerships: Script validated");
                }
            }
        }
        return ownershipsToUpdate;
    }

    /**
     * Get the next state from the signature of a common function.
     *
     * @param contractId:                   id of the contract from which to get the next state.
     * @param sourceState:                  source state of the common function.
     * @param party:                        party name of the common function.
     * @param functionName:                 name of the function.
     * @param argumentTypes:                list of argument types for the common function.
     * @return                              return the next state got from the signature of the common function.
     * @throws IOException:                 throws when an error occur while opening or closing the connection with the storage.
     * @throws ContractNotFoundException:   throws when the contract id is not referred to any contract saved in the storage.
     */
    private DfaState getNextStateFromCommonFunction(
            String contractId,
            DfaState sourceState,
            String party,
            String functionName,
            ArrayList<String> argumentTypes
    ) throws IOException, ContractNotFoundException {
        // Load all the bytecode
        Contract contract = contractsStorage.getContract(contractId);
        String bytecode = contract.getBytecode();
        String[] instructions = bytecode.split("\n");

        for (int i = 0; i < instructions.length; i++) {
            String[] instruction = instructions[i].trim().split(" ");

            if (instruction[0].equals("fn") &&
                    instruction[1].equals(sourceState.getName()) &&
                    instruction[2].equals(party) &&
                    instruction[3].equals(functionName)
            ) {
                if (instruction.length == 6 && argumentTypes != null && argumentTypes.size() > 0) {
                    // Check the arguments types
                    boolean areArgumentTypesCorrect = true;
                    String[] argumentTypesFromFunction = instruction[5].split(",");

                    if (argumentTypesFromFunction.length == argumentTypes.size()) {
                        // Check the arguments types
                        for (int j = 0; j < argumentTypesFromFunction.length; j++) {
                            String argumentTypeFromFunction = argumentTypesFromFunction[j];

                            if (!argumentTypeFromFunction.equals("*")) {
                                if (!argumentTypeFromFunction.equals(argumentTypes.get(j))) {
                                    areArgumentTypesCorrect = false;
                                }
                            }
                        }

                        if (areArgumentTypesCorrect) {
                            return new DfaState(instruction[4]);
                        }
                    }
                } else {
                    return new DfaState(instruction[4]);
                }
            }
        }
        return null;
    }

    /**
     * Get the next state from the signature of the obligation function.
     *
     * @param contractId:                   id of the contract from which to get the next state.
     * @param sourceState:                  source state of the obligation function.
     * @param obligationFunctionName:       name of the obligation function to load.
     * @return                              return the next state got from the signature of the obligation function.
     * @throws IOException:                 throws when an error occur while opening or closing the connection with the storage.
     * @throws ContractNotFoundException:   throws when the contract id is not referred to any contract saved in the storage.
     */
    private DfaState getNextStateFromObligationFunction(
            String contractId,
            DfaState sourceState,
            String obligationFunctionName
    ) throws IOException, ContractNotFoundException {
        // Load all the bytecode
        Contract contract = contractsStorage.getContract(contractId);
        String bytecode = contract.getBytecode();
        String[] instructions = bytecode.split("\n");

        for (int i = 0; i < instructions.length; i++) {
            String[] instruction = instructions[i].trim().split(" ");

            if (instruction[0].equals("obligation") &&
                    instruction[1].equals(sourceState.getName()) &&
                    instruction[2].equals(obligationFunctionName)
            ) {
                return new DfaState(instruction[3]);
            }
        }
        return null;
    }
}

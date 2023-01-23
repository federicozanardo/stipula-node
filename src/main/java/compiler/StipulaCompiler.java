package compiler;

import constants.Constants;
import event.EventTriggerHandler;
import lib.datastructures.Pair;
import models.address.Address;
import models.contract.Contract;
import models.dto.requests.contract.deploy.DeployContract;
import models.dto.requests.event.EventTriggerRequest;
import models.dto.requests.event.EventTriggerSchedulingRequest;
import models.dto.responses.Response;
import models.dto.responses.SuccessDataResponse;
import shared.SharedMemory;
import storage.ContractsStorage;
import vm.dfa.ContractCallByParty;
import vm.dfa.DfaState;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;

public class StipulaCompiler extends Thread {
    private final Thread clientHandler;
    private final DeployContract contractToDeploy;
    private final EventTriggerHandler eventTriggerHandler;
    private final SharedMemory<Response> sharedMemory;
    private final ContractsStorage contractsStorage;

    public StipulaCompiler(
            String name,
            Thread clientHandler,
            DeployContract contractToDeploy,
            EventTriggerHandler eventTriggerHandler,
            SharedMemory<Response> sharedMemory,
            ContractsStorage contractsStorage
    ) {
        super(name);
        this.clientHandler = clientHandler;
        this.contractToDeploy = contractToDeploy;
        this.eventTriggerHandler = eventTriggerHandler;
        this.sharedMemory = sharedMemory;
        this.contractsStorage = contractsStorage;
    }

    @Override
    public void run() {
        boolean result = false;

        System.out.println("StipulaCompiler: Compiling...");
        try {
            result = this.compile();
        } catch (NoSuchAlgorithmException e) {
            // TODO: Return a ErrorResponse: Error while compiling
            // throw new RuntimeException(e);
        }

        if (!result) {
            // TODO: Return a ErrorResponse: Error while compiling
            return;
        }

        System.out.println("StipulaCompiler: Compilation successful");

        String bytecode = readProgram(Constants.EXAMPLES_PATH + "contract1.sb");

        // Load the DFA
        Address lenderAddr;
        try {
            lenderAddr = new Address("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCo/GjVKS+3gAA55+kko41yINdOcCLQMSBQyuTTkKHE1mhu/TgOpivM0wLPsSga8hQMr3+v3aR0IF/vfCRf6SdiXmWx/jflmEXtnT6fkGcnV6dGNUpHWXSpwUIDt0N88jfnEqekx4S+KDCKg99sGEeHeT65fKS8lB0gjHMt9AOriwIDAQAB");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        Address borrowerAddr;
        try {
            borrowerAddr = new Address("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDErzzgD2ZslZxciFAiX3/ot7lrkZDw4148jFZrsDZPE6CVs9xXFSHGgy/mFvIFLXhnChO6Nyd2be3lbgeavLMCMVUiTStXr117Km17keWpb3sItkKKsLFBOcIIU8XXowI/OhzQN2XPZYESHgjdQ5vwEj2YyueiS7WKP94YWz/pswIDAQAB");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        ArrayList<Address> authorizedParties1 = new ArrayList<Address>();
        authorizedParties1.add(lenderAddr);
        ArrayList<Address> authorizedParties2 = new ArrayList<Address>();
        authorizedParties2.add(borrowerAddr);

        ArrayList<Pair<String, DfaState>> transitions = new ArrayList<>();
        transitions.add(new Pair<String, DfaState>("Inactive", new ContractCallByParty("Proposal", authorizedParties1)));
        transitions.add(new Pair<String, DfaState>("Proposal", new ContractCallByParty("Using", authorizedParties2)));
        transitions.add(new Pair<String, DfaState>("Using", new ContractCallByParty("End", authorizedParties2)));

        Contract contract = new Contract("", bytecode, "Inactive", "End", transitions);

        String contractId;
        try {
            contractId = contractsStorage.addContract(contract);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("StipulaCompiler: contractId = " + contractId);

        System.out.println("StipulaCompiler: Set up event triggers...");

        for (int i = 0; i < 5; i++) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            EventTriggerRequest request = new EventTriggerRequest(
                    "a" + i,
                    "b" + i,
                    "call" + i
            );
            EventTriggerSchedulingRequest schedulingRequest = new EventTriggerSchedulingRequest(request, 5);
            this.eventTriggerHandler.addTask(schedulingRequest);
            System.out.println("StipulaCompiler: added " + schedulingRequest);
        }

        System.out.println("StipulaCompiler: get => " + this.sharedMemory.get(this.getName()));

        if (this.sharedMemory.containsKey(this.clientHandler.getName())) {
            this.sharedMemory.set(
                    this.clientHandler.getName(),
                    new SuccessDataResponse("ack from StipulaCompiler\nThe contract id is " + contractId)
            );

            System.out.println("StipulaCompiler: Now I'll notify the thread " + this.clientHandler.getName());
            synchronized (this.clientHandler) {
                this.clientHandler.notify();
            }

            System.out.println("StipulaCompiler: Bye bye!");
        } else {
            System.out.println("StipulaCompiler: Oh no! There is no reference in the shared space for this thread " + this.clientHandler.getName());
        }
    }

    private boolean compile() throws NoSuchAlgorithmException {
        this.setupContract();
        System.out.println("StipulaCompiler:compile => " + this.contractToDeploy);
        return true;
    }

    private void setupContract() throws NoSuchAlgorithmException {
        String bytecode = readProgram(Constants.EXAMPLES_PATH + "contract1.sb");

        // Load the DFA
        Address lenderAddr = new Address("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCo/GjVKS+3gAA55+kko41yINdOcCLQMSBQyuTTkKHE1mhu/TgOpivM0wLPsSga8hQMr3+v3aR0IF/vfCRf6SdiXmWx/jflmEXtnT6fkGcnV6dGNUpHWXSpwUIDt0N88jfnEqekx4S+KDCKg99sGEeHeT65fKS8lB0gjHMt9AOriwIDAQAB");
        Address borrowerAddr = new Address("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDErzzgD2ZslZxciFAiX3/ot7lrkZDw4148jFZrsDZPE6CVs9xXFSHGgy/mFvIFLXhnChO6Nyd2be3lbgeavLMCMVUiTStXr117Km17keWpb3sItkKKsLFBOcIIU8XXowI/OhzQN2XPZYESHgjdQ5vwEj2YyueiS7WKP94YWz/pswIDAQAB");

        ArrayList<Address> authorizedParties1 = new ArrayList<Address>();
        authorizedParties1.add(lenderAddr);
        ArrayList<Address> authorizedParties2 = new ArrayList<Address>();
        authorizedParties2.add(borrowerAddr);

        ArrayList<Pair<String, DfaState>> transitions = new ArrayList<>();
        transitions.add(new Pair<String, DfaState>(
                        "Inactive",
                        new ContractCallByParty("Proposal", authorizedParties1)
                )
        );
        transitions.add(new Pair<String, DfaState>(
                        "Proposal",
                        new ContractCallByParty("Using", authorizedParties2)
                )
        );
        transitions.add(new Pair<String, DfaState>(
                        "Using",
                        new ContractCallByParty("End", authorizedParties2)
                )
        );

        Contract contract = new Contract(
                "",
                bytecode,
                "Inactive",
                "End",
                transitions
        );

        // Save the contract
        // String contractId = contractsStorage.addContract(contract);

        // System.out.println("setupContract: contractId = " + contractId);
    }

    private String readProgram(String pathname) {
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
}

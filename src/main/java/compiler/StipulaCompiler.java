package compiler;

import lib.datastructures.Pair;
import models.address.Address;
import models.contract.Contract;
import models.dto.requests.contract.deploy.DeployContract;
import models.dto.responses.Response;
import models.dto.responses.SuccessDataResponse;
import shared.SharedMemory;
import storage.ContractsStorage;
import vm.dfa.ContractCallByEvent;
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
    private final SharedMemory<Response> sharedMemory;
    private final ContractsStorage contractsStorage;

    public StipulaCompiler(
            String name,
            Thread clientHandler,
            DeployContract contractToDeploy,
            SharedMemory<Response> sharedMemory,
            ContractsStorage contractsStorage
    ) {
        super(name);
        this.clientHandler = clientHandler;
        this.contractToDeploy = contractToDeploy;
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

        // String bytecode = readProgram(Constants.EXAMPLES_PATH + "contract1.sb");

        String bytecode = "contract c60050c5bb40e4a48df80f1b390ba206f5dcbdcdef5cba561a2dc43cf715989e\n" +
                "fn agreement\n" +
                "global:\n" +
                "GINST addr Lender\n" +
                "GINST addr Borrower\n" +
                "GINST asset wallet 2 1a3e31ad-5032-484c-9cdd-f1ed3bd760ac\n" +
                "GINST float cost 2\n" +
                "GINST time rent_time\n" +
                "GINST int use_code\n" +
                "args:\n" +
                "PUSH addr :Lender\n" +
                "GSTORE Lender\n" +
                "PUSH addr :Borrower\n" +
                "GSTORE Borrower\n" +
                "PUSH float :cost\n" +
                "GSTORE cost\n" +
                "PUSH time :rent_time\n" +
                "GSTORE rent_time\n" +
                "start:\n" +
                "HALT\n" +
                "fn offer Lender Proposal\n" +
                "args:\n" +
                "PUSH int :z\n" +
                "AINST int z\n" +
                "ASTORE z\n" +
                "start:\n" +
                "ALOAD z\n" +
                "GSTORE use_code\n" +
                "HALT\n" +
                "fn accept Borrower Using\n" +
                "args:\n" +
                "PUSH asset :y\n" +
                "AINST asset y 2 1a3e31ad-5032-484c-9cdd-f1ed3bd760ac\n" +
                "ASTORE y\n" +
                "start:\n" +
                "ALOAD y\n" +
                "GLOAD cost\n" +
                "ISEQ\n" +
                "JMPIF if_branch\n" +
                "RAISE AMOUNT_NOT_EQUAL\n" +
                "JMP end\n" +
                "if_branch:\n" +
                "ALOAD y\n" +
                "GLOAD wallet\n" +
                "DEPOSIT wallet\n" +
                "PUSH time now\n" +
                "GLOAD rent_time\n" +
                "ADD\n" +
                "TRIGGER accept_obl_1\n" +
                "end:\n" +
                "HALT\n" +
                "fn end Borrower End\n" +
                "start:\n" +
                "PUSH float 100 2\n" +
                "GLOAD wallet\n" +
                "MUL\n" +
                "GLOAD wallet\n" +
                "GLOAD Lender\n" +
                "WITHDRAW wallet\n" +
                "HALT\n" +
                "obligation accept_obl_1 End\n" +
                "start:\n" +
                "PUSH float 100 2\n" +
                "GLOAD wallet\n" +
                "MUL\n" +
                "GLOAD wallet\n" +
                "GLOAD Lender\n" +
                "WITHDRAW wallet\n" +
                "HALT";

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
        transitions.add(new Pair<String, DfaState>("Using", new ContractCallByEvent("End", "accept_obl_1")));

        ArrayList<String> endStates = new ArrayList<>();
        endStates.add("End");

        Contract contract = new Contract(
                "",
                bytecode,
                "Inactive",
                endStates,
                transitions
        );

        String contractId;
        try {
            contractId = contractsStorage.addContract(contract);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("StipulaCompiler: contractId = " + contractId);

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
        //this.setupContract();
        System.out.println("StipulaCompiler:compile => " + this.contractToDeploy);
        return true;
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

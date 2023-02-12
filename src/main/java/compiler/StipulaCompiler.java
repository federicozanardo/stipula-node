package compiler;

import lib.datastructures.Triple;
import models.contract.Contract;
import models.dto.requests.contract.deploy.DeployContract;
import storage.ContractsStorage;
import vm.dfa.states.DfaState;
import vm.dfa.states.FinalStates;
import vm.dfa.transitions.ContractCallByEvent;
import vm.dfa.transitions.ContractCallByParty;
import vm.dfa.transitions.TransitionData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class StipulaCompiler {
    private final DeployContract contractToDeploy;
    private final ContractsStorage contractsStorage;

    public StipulaCompiler(DeployContract contractToDeploy, ContractsStorage contractsStorage) {
        this.contractToDeploy = contractToDeploy;
        this.contractsStorage = contractsStorage;
    }

    public String compile() {
        //this.setupContract();

        System.out.println("compile: " + this.contractToDeploy);

        // boolean result = false;

        System.out.println("compile: Compiling...");
        // TODO: compile method
        // TODO: Return a ErrorResponse: Error while compiling

        /*if (!result) {
            // TODO: Return a ErrorResponse: Error while compiling
            return;
        }*/

        System.out.println("compile: Compilation successful");

        // String bytecode = readProgram(Constants.EXAMPLES_PATH + "contract1.sb");

        String bytecode = "contract c60050c5bb40e4a48df80f1b390ba206f5dcbdcdef5cba561a2dc43cf715989e\n" +
                "fn agreement Lender,Borrower Inactive float,time\n" +
                "global:\n" +
                "GINST party Lender\n" +
                "GINST party Borrower\n" +
                "GINST asset wallet 2 1a3e31ad-5032-484c-9cdd-f1ed3bd760ac\n" +
                "GINST float cost 2\n" +
                "GINST time rent_time\n" +
                "GINST int use_code\n" +
                "args:\n" +
                "PUSH party :Lender\n" +
                "GSTORE Lender\n" +
                "PUSH party :Borrower\n" +
                "GSTORE Borrower\n" +
                "PUSH float :cost\n" +
                "GSTORE cost\n" +
                "PUSH time :rent_time\n" +
                "GSTORE rent_time\n" +
                "start:\n" +
                "HALT\n" +
                "fn Inactive Lender offer Proposal int\n" +
                "args:\n" +
                "PUSH int :z\n" +
                "AINST int z\n" +
                "ASTORE z\n" +
                "start:\n" +
                "ALOAD z\n" +
                "GSTORE use_code\n" +
                "HALT\n" +
                "fn Proposal Borrower accept Using asset\n" +
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
                "fn Using Borrower end End\n" +
                "start:\n" +
                "PUSH float 100 2\n" +
                "GLOAD wallet\n" +
                "MUL\n" +
                "GLOAD wallet\n" +
                "GLOAD Lender\n" +
                "WITHDRAW wallet\n" +
                "HALT\n" +
                "obligation Using accept_obl_1 End\n" +
                "start:\n" +
                "PUSH float 100 2\n" +
                "GLOAD wallet\n" +
                "MUL\n" +
                "GLOAD wallet\n" +
                "GLOAD Lender\n" +
                "WITHDRAW wallet\n" +
                "HALT";

        // Create an DFA instance

        // Set the arguments for each function
        ArrayList<String> offerArgs = new ArrayList<>();
        offerArgs.add("int");
        ArrayList<String> acceptArgs = new ArrayList<>();
        acceptArgs.add("asset");
        ArrayList<String> endArgs = new ArrayList<>();

        // Set the DFA transitions
        ArrayList<Triple<DfaState, DfaState, TransitionData>> transitions = new ArrayList<>();
        transitions.add(new Triple<DfaState, DfaState, TransitionData>(
                new DfaState("Inactive"),
                new DfaState("Proposal"),
                new ContractCallByParty("offer", "Lender", offerArgs)));
        transitions.add(new Triple<DfaState, DfaState, TransitionData>(
                new DfaState("Proposal"),
                new DfaState("Using"),
                new ContractCallByParty("accept", "Borrower", acceptArgs)));
        transitions.add(new Triple<DfaState, DfaState, TransitionData>(
                new DfaState("Using"),
                new DfaState("End"),
                new ContractCallByParty("end", "Borrower", endArgs)));
        transitions.add(new Triple<DfaState, DfaState, TransitionData>(
                new DfaState("Using"),
                new DfaState("End"),
                new ContractCallByEvent("accept_obl_1")));

        // Set the initial state
        DfaState initialState = new DfaState("Inactive");

        // Set the final state
        HashSet<DfaState> acceptanceStates = new HashSet<>();
        acceptanceStates.add(new DfaState("End"));
        FinalStates finalStates = new FinalStates(acceptanceStates, null);

        // Create the contract
        Contract contract = new Contract(contractToDeploy.getSourceCode(), bytecode, initialState, finalStates, transitions);

        // Store the new contract
        String contractId;
        try {
            contractId = contractsStorage.saveContract(contract);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("compile: contractId = " + contractId);
        return contractId;
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

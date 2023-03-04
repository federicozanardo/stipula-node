package compiler;

import compiler.ast.StipulaCompiler;
import compiler.ast.Type;
import compiler.ast.TypeChecking;
import compiler.ast.TypeInference;
import compiler.lexer.StipulaLexer;
import compiler.parser.StipulaParser;
import constants.Constants;
import lib.datastructures.Pair;
import lib.datastructures.Triple;
import models.contract.Contract;
import models.dto.requests.contract.deploy.DeployContract;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import storage.ContractsStorage;
import vm.dfa.states.DfaState;
import vm.dfa.states.FinalStates;
import vm.dfa.transitions.ContractCallByEvent;
import vm.dfa.transitions.ContractCallByParty;
import vm.dfa.transitions.TransitionData;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class Compiler {
    private final DeployContract contractToDeploy;
    private final ContractsStorage contractsStorage;

    public Compiler(DeployContract contractToDeploy, ContractsStorage contractsStorage) {
        this.contractToDeploy = contractToDeploy;
        this.contractsStorage = contractsStorage;
    }

    public static void main(String[] args) throws Exception {
        String fileName = Constants.EXAMPLES_PATH + "bike_rental.stipula";
        // String fileName = Constants.EXAMPLES_PATH + "bike_rental_full.stipula";
        FileInputStream is = new FileInputStream(fileName);
        ANTLRInputStream input = new ANTLRInputStream(is);
        StipulaLexer lexer = new StipulaLexer((CharStream) input);
        CommonTokenStream tokens = new CommonTokenStream((TokenSource) lexer);
        StipulaParser parser = new StipulaParser((TokenStream) tokens);

        if (parser.getNumberOfSyntaxErrors() == 0) {
            ParseTree parseTree = parser.prog();

            // TYPE CHECKING
            TypeChecking typeChecking = new TypeChecking();
            @SuppressWarnings("unchecked")
            Map<Pair<String, Integer>, Type> types = (Map<Pair<String, Integer>, Type>) typeChecking.visit(parseTree);
            System.out.println("types => " + types);
            typeChecking.printMap();
            ArrayList<Pair<String, ArrayList<Pair<String, Type>>>> functionParameters = typeChecking.getFunctionParameters();
            System.out.println("functionParameters => " + functionParameters);
            ArrayList<String> contractNames = typeChecking.getContractNames();
            System.out.println("contractNames => " + contractNames);
            System.out.println("TYPE CHECKING:");
            System.out.println("==================");
            TypeInference typeInference = new TypeInference(types, contractNames, functionParameters);
            typeInference.printMap();
            System.out.println("function types: " + typeInference.getFunctionTypes());
            Map<String, ArrayList<String>> functionTypes = typeInference.getFunctionTypes();
            System.out.println("==================");

            // INTERPRETER
            // Interpreter codeInterpreter = new Interpreter();
            // Object program = codeInterpreter.visit(parseTree);
            // ((Program) program).runProgram(typeinferencer);
            Map<Pair<String, Integer>, Type> globalVariables = typeInference.getGlobalVariables();
            System.out.println("globalVariables => " + globalVariables);

            // Compile
            StipulaCompiler stipulaCompiler = new StipulaCompiler(globalVariables, functionTypes);
            String bytecode = (String) stipulaCompiler.visit(parseTree);

            // Set the initial state
            DfaState initialState = stipulaCompiler.getInitialState();

            // Set the final state
            FinalStates finalStates = stipulaCompiler.getFinalStates();

            // Set the DFA transitions
            ArrayList<Triple<DfaState, DfaState, TransitionData>> transitions = stipulaCompiler.getTransitions();
            System.out.println("transitions => ");
            for (Triple<DfaState, DfaState, TransitionData> transition : transitions) {
                System.out.println("source => " + transition.getFirst());
                System.out.println("destination => " + transition.getSecond());
                System.out.println("transition data => " + transition.getThird() + "\n");
            }

            // Create the contract
            Contract contract = new Contract(
                    //contractToDeploy.getSourceCode()
                    "",
                    bytecode,
                    initialState,
                    finalStates,
                    transitions
            );
        }
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
                "fn agreement Lender,Borrower Inactive real,time\n" +
                "global:\n" +
                "GINST party Lender\n" +
                "GINST party Borrower\n" +
                "GINST asset wallet 2 1a3e31ad-5032-484c-9cdd-f1ed3bd760ac\n" +
                "GINST real cost 2\n" +
                "GINST time rent_time\n" +
                "GINST int use_code\n" +
                "args:\n" +
                "PUSH party :Lender\n" +
                "GSTORE Lender\n" +
                "PUSH party :Borrower\n" +
                "GSTORE Borrower\n" +
                "PUSH real :cost\n" +
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
                "PUSH real 100 2\n" +
                "GLOAD wallet\n" +
                "MUL\n" +
                "GLOAD wallet\n" +
                "GLOAD Lender\n" +
                "WITHDRAW wallet\n" +
                "HALT\n" +
                "obligation Using accept_obl_1 End\n" +
                "start:\n" +
                "PUSH real 100 2\n" +
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
}

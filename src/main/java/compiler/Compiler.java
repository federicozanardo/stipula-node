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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

    public String compile() throws IOException {
        System.out.println("compile: " + this.contractToDeploy);
        System.out.println("compile: Compiling...");

        InputStream is = new ByteArrayInputStream(contractToDeploy.getSourceCode().getBytes(StandardCharsets.UTF_8));
        ANTLRInputStream input = new ANTLRInputStream(is);
        StipulaLexer lexer = new StipulaLexer((CharStream) input);
        CommonTokenStream tokens = new CommonTokenStream((TokenSource) lexer);
        StipulaParser parser = new StipulaParser((TokenStream) tokens);

        if (parser.getNumberOfSyntaxErrors() == 0) {
            ParseTree parseTree = parser.prog();

            // Type checking
            TypeChecking typeChecking = new TypeChecking();
            @SuppressWarnings("unchecked")
            Map<Pair<String, Integer>, Type> types = (Map<Pair<String, Integer>, Type>) typeChecking.visit(parseTree);
            ArrayList<Pair<String, ArrayList<Pair<String, Type>>>> functionParameters = typeChecking.getFunctionParameters();
            ArrayList<String> contractNames = typeChecking.getContractNames();

            // Type inference
            TypeInference typeInference = new TypeInference(types, contractNames, functionParameters);

            // Get the types of function arguments
            Map<String, ArrayList<String>> functionTypes = typeInference.getFunctionTypes();

            // Get global variables
            Map<Pair<String, Integer>, Type> globalVariables = typeInference.getGlobalVariables();

            // Compile
            StipulaCompiler stipulaCompiler = new StipulaCompiler(globalVariables, functionTypes);
            String bytecode = (String) stipulaCompiler.visit(parseTree);
            System.out.println("compile: Compilation successful");

            // Set the initial state
            DfaState initialState = stipulaCompiler.getInitialState();

            // Set the final state
            FinalStates finalStates = stipulaCompiler.getFinalStates();

            // Set the DFA transitions
            ArrayList<Triple<DfaState, DfaState, TransitionData>> transitions = stipulaCompiler.getTransitions();

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

        return null; // FIXME
        // TODO: Return a ErrorResponse: Error while compiling
    }
}

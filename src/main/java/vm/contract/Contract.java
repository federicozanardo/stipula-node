package vm.contract;

import lib.datastructures.Pair;
import vm.dfa.State;

import java.io.Serializable;
import java.util.ArrayList;

public class Contract implements Serializable {
    private final String sourceCode;
    private final String bytecode;
    private final String initialState;
    private final String endState; // acceptanceState
    private final ArrayList<Pair<String, State>> transitions;

    public Contract(String sourceCode, String bytecode, String initialState, String endState, ArrayList<Pair<String, State>> transitions) {
        this.sourceCode = sourceCode;
        this.bytecode = bytecode;
        this.initialState = initialState;
        this.endState = endState;
        this.transitions = transitions;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public String getBytecode() {
        return bytecode;
    }

    public String getInitialState() {
        return initialState;
    }

    public String getEndState() {
        return endState;
    }

    public ArrayList<Pair<String, State>> getTransitions() {
        return transitions;
    }
}

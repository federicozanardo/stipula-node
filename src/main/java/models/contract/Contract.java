package models.contract;

import lib.datastructures.Pair;
import vm.dfa.DfaState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Contract implements Serializable {
    private final String sourceCode;
    private final String bytecode;
    private final String initialState;
    private final HashSet<String> endStates;
    private final ArrayList<Pair<String, DfaState>> transitions;

    public Contract(String sourceCode,
                    String bytecode,
                    String initialState,
                    HashSet<String> endStates,
                    ArrayList<Pair<String, DfaState>> transitions
    ) {
        this.sourceCode = sourceCode;
        this.bytecode = bytecode;
        this.initialState = initialState;
        this.endStates = endStates;
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

    public HashSet<String> getEndStates() {
        return endStates;
    }

    public ArrayList<Pair<String, DfaState>> getTransitions() {
        return transitions;
    }
}

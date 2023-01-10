package vm.dfa;

import lib.datastructures.Pair;
import vm.types.address.Address;

import java.io.Serializable;
import java.util.ArrayList;

public class DeterministicFiniteAutomata implements Serializable {
    private final String endState; // acceptanceState
    private String currentState;
    private final ArrayList<Pair<String, State>> transitions;

    public DeterministicFiniteAutomata(String initialState, String endState, ArrayList<Pair<String, State>> transitions) {
        this.currentState = initialState;
        this.endState = endState;
        this.transitions = transitions;
    }

    public boolean isNextState(String nextState, Address party) {
        int i = 0;
        boolean found = false;

        while (i < this.transitions.size() && !found) {
            Pair<String, State> state = this.transitions.get(i);

            // Find the source state
            if (this.currentState.equals(state.getFirst())) {

                // Check if it is the correct destination state
                if (state.getSecond().getName().equals(nextState)) {

                    // Check the instance
                    if (state.getSecond() instanceof ContractCallByParty) {
                        ContractCallByParty callByParty = (ContractCallByParty) state.getSecond();

                        // Check if the request has been made by an authorized party
                        if (callByParty.getAuthorizedParties().contains(party)) {
                            found = true;
                        }
                    }
                }
            }
            i++;
        }

        return found;
    }

    public boolean isNextState(String nextState, boolean isTransitionByEvent) {
        int i = 0;
        boolean found = false;

        while (i < this.transitions.size() && !found) {
            Pair<String, State> state = this.transitions.get(i);

            // Find the source state
            if (this.currentState.equals(state.getFirst())) {

                // Check if it is the correct destination state
                if (state.getSecond().getName().equals(nextState)) {

                    // Check the instance
                    /*if (state.getSecond() instanceof ContractCallByEvent) {
                        // TODO: before implementing it, develop the Events :)
                        *//*ContractCallByParty callByParty = (ContractCallByParty) state.getSecond();

                        // Check if the request has been made by an authorized party
                        if (callByParty.getAuthorizedParties().contains(party)) {
                            found = true;
                        }*//*
                    }*/
                }
            }
            i++;
        }

        return found;
    }

    public void nextState(String nextState, Address party) {
        if (this.isNextState(nextState, party)) {
            this.currentState = nextState;
        }
    }

    public void nextState(String nextState, boolean isTransitionByEvent) {
        if (this.isNextState(nextState, isTransitionByEvent)) {
            this.currentState = nextState;
        }
    }

    public boolean isCurrentStateEndState() {
        return this.currentState.equals(this.endState);
    }

    public boolean isEndState(String state) {
        return state.equals(this.endState);
    }

    public String getCurrentState() {
        return currentState;
    }
}

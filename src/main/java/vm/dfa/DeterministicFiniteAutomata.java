package vm.dfa;

import lib.datastructures.Pair;
import models.address.Address;

import java.io.Serializable;
import java.util.ArrayList;

public class DeterministicFiniteAutomata implements Serializable {
    private final ArrayList<String> endStates;
    private String currentState;
    private final ArrayList<Pair<String, DfaState>> transitions;

    public DeterministicFiniteAutomata(
            String initialState,
            ArrayList<String> endStates,
            ArrayList<Pair<String, DfaState>> transitions
    ) {
        this.currentState = initialState;
        this.endStates = endStates;
        this.transitions = transitions;
    }

    public boolean isNextState(String nextState, Address party) {
        int i = 0;
        boolean found = false;

        while (i < this.transitions.size() && !found) {
            Pair<String, DfaState> state = this.transitions.get(i);

            // Look only at ContractCallByParty objects
            if (state.getSecond() instanceof ContractCallByParty) {
                // Find the source state
                if (this.currentState.equals(state.getFirst())) {

                    // Check if it is the correct destination state
                    if (state.getSecond().getName().equals(nextState)) {
                        ContractCallByParty callByParty = (ContractCallByParty) state.getSecond();

                        // Check if the request has been made by an authorized party
                        for (Address authorizedParty : callByParty.getAuthorizedParties()) {
                            if (authorizedParty.getAddress().equals(party.getAddress())) {
                                found = true;
                            }
                        }
                    }
                }
            }
            i++;
        }

        return found;
    }

    public boolean isNextState(String nextState, String obligationFunctionName) {
        int i = 0;
        boolean found = false;

        while (i < this.transitions.size() && !found) {
            Pair<String, DfaState> state = this.transitions.get(i);

            // Look only at ContractCallByEvent objects
            if (state.getSecond() instanceof ContractCallByEvent) {
                // Find the source state
                if (this.currentState.equals(state.getFirst())) {

                    // Check if it is the correct destination state
                    if (state.getSecond().getName().equals(nextState)) {
                        ContractCallByEvent callByEvent = (ContractCallByEvent) state.getSecond();

                        // Check if the request has been made by the correct obligation
                        if (callByEvent.getObligationFunctionName().equals(obligationFunctionName)) {
                            found = true;
                        }
                    }
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

    public void nextState(String nextState, String obligationFunctionName) {
        if (this.isNextState(nextState, obligationFunctionName)) {
            this.currentState = nextState;
        }
    }

    public boolean isCurrentStateEndState() {
        return this.endStates.contains(this.currentState);
    }

    public boolean isEndState(String state) {
        return this.endStates.contains(state);
    }

    public String getCurrentState() {
        return currentState;
    }

    @Override
    public String toString() {
        return "DeterministicFiniteAutomata{" +
                "endState='" + endStates + '\'' +
                ", currentState='" + currentState + '\'' +
                ", transitions=" + transitions +
                '}';
    }
}

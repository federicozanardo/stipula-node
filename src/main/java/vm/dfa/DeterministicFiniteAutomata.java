package vm.dfa;

import lib.datastructures.Pair;
import models.address.Address;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DeterministicFiniteAutomata implements Serializable {
    private final HashSet<String> endStates;
    private String currentState;
    private final ArrayList<Pair<String, DfaState>> transitions;

    public DeterministicFiniteAutomata(
            String initialState,
            HashSet<String> endStates,
            ArrayList<Pair<String, DfaState>> transitions
    ) {
        this.currentState = initialState;
        this.endStates = endStates;
        this.transitions = transitions;
    }

    /**
     * Check if the given state is the next state of the current state, given a party.
     *
     * @param nextState: state to check if it is the next state of the current state.
     * @param party: the party that made the request.
     * @return true, if the given state is the next state of the current state; false, otherwise.
     */
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

    /**
     * Check if the given state is the next state of the current state, given an obligation function name.
     *
     * @param nextState: state to check if it is the next state of the current state.
     * @param obligationFunctionName: obligation to call.
     * @return true, if the given state is the next state of the current state; false, otherwise.
     */
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

    /**
     * Update the state machine.
     *
     * @param nextState: the next state of the current state.
     * @param party: the party that made the request.
     */
    public void nextState(String nextState, Address party) {
        if (this.isNextState(nextState, party)) {
            this.currentState = nextState;
        }
    }

    /**
     * Update the state machine.
     *
     * @param nextState: the next state of the current state.
     * @param obligationFunctionName: obligation to call.
     */
    public void nextState(String nextState, String obligationFunctionName) {
        if (this.isNextState(nextState, obligationFunctionName)) {
            this.currentState = nextState;
        }
    }

    /**
     * Check if the current state belongs to the end-states set.
     *
     * @return true, if the current state belongs to the end-states set; false otherwise.
     */
    public boolean isCurrentStateEndState() {
        return this.endStates.contains(this.currentState);
    }

    /**
     * Check if the state, given in input, belongs to the end-states set.
     *
     * @param state: the state to check if it belongs to the end-states set.
     * @return true, if the given state belongs to the end-states set; false otherwise.
     */
    public boolean isEndState(String state) {
        return this.endStates.contains(state);
    }

    /**
     * Return the current state of the state machine.
     *
     * @return the current state.
     */
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

package models.contract;

import models.address.Address;
import vm.dfa.DeterministicFiniteAutomata;
import vm.types.Type;

import java.io.Serializable;
import java.util.HashMap;

public class ContractInstance implements Serializable {
    private final String contractId;
    private final DeterministicFiniteAutomata stateMachine;
    private final HashMap<String, Address> parties;
    private final HashMap<String, Type> globalSpace;

    public ContractInstance(String contractId, DeterministicFiniteAutomata stateMachine, HashMap<String, Address> parties) {
        this.contractId = contractId;
        this.parties = parties;
        this.stateMachine = stateMachine;
        this.globalSpace = new HashMap<>();
    }

    public String getContractId() {
        return contractId;
    }

    public DeterministicFiniteAutomata getStateMachine() {
        return stateMachine;
    }

    public HashMap<String, Address> getParties() {
        return parties;
    }

    public HashMap<String, Type> getGlobalSpace() {
        return globalSpace;
    }

    @Override
    public String toString() {
        return "ContractInstance{" +
                "contractId='" + contractId + '\'' +
                ", stateMachine=" + stateMachine +
                ", parties=" + parties +
                ", globalSpace=" + globalSpace +
                '}';
    }
}

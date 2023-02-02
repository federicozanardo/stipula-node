package models.contract;

import vm.dfa.DeterministicFiniteAutomata;
import vm.types.Type;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class ContractInstance implements Serializable {
    private final String contractId;
    private final String contractInstanceId;
    private final DeterministicFiniteAutomata stateMachine;
    private final HashMap<String, Type> globalSpace;

    public ContractInstance(String contractId, DeterministicFiniteAutomata stateMachine) {
        this.contractId = contractId;
        this.contractInstanceId = UUID.randomUUID().toString();
        this.stateMachine = stateMachine;
        this.globalSpace = new HashMap<>();
    }

    public String getContractId() {
        return contractId;
    }

    public String getContractInstanceId() {
        return contractInstanceId;
    }

    public DeterministicFiniteAutomata getStateMachine() {
        return stateMachine;
    }

    public HashMap<String, Type> getGlobalSpace() {
        return globalSpace;
    }

    @Override
    public String toString() {
        return "ContractInstance{" +
                "contractId='" + contractId + '\'' +
                ", contractInstanceId='" + contractInstanceId + '\'' +
                ", stateMachine=" + stateMachine +
                ", globalSpace=" + globalSpace +
                '}';
    }
}

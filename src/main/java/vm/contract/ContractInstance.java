package vm.contract;

import vm.dfa.DeterministicFiniteAutomata;
import vm.types.Type;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class ContractInstance implements Serializable {
    private final String contractId;
    private final String instanceId;
    private final DeterministicFiniteAutomata stateMachine;
    private final HashMap<String, Type> globalVariables;

    public ContractInstance(String contractId, DeterministicFiniteAutomata stateMachine) {
        this.contractId = contractId;
        this.instanceId = UUID.randomUUID().toString();
        this.stateMachine = stateMachine;
        this.globalVariables = new HashMap<>();
    }

    public String getContractId() {
        return contractId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public DeterministicFiniteAutomata getStateMachine() {
        return stateMachine;
    }

    public HashMap<String, Type> getGlobalVariables() {
        return globalVariables;
    }

    @Override
    public String toString() {
        return "ContractInstance{" +
                "contractId='" + contractId + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", stateMachine=" + stateMachine +
                ", globalVariables=" + globalVariables +
                '}';
    }
}

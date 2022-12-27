package vm.contract;

import vm.types.Type;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class ContractInstance implements Serializable {
    private final String contractId;
    private final String instanceId;
    private String currentState;
    private HashMap<String, Type> globalVariables;

    public ContractInstance(String contractId, String initialState) {
        this.instanceId = UUID.randomUUID().toString();
        this.contractId = contractId;
        this.currentState = initialState;
        this.globalVariables = new HashMap<>();
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getContractId() {
        return contractId;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public HashMap<String, Type> getGlobalVariables() {
        return globalVariables;
    }

    public void setGlobalVariables(HashMap<String, Type> globalVariables) {
        this.globalVariables = globalVariables;
    }
}

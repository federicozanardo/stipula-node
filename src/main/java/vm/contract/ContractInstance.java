package vm.contract;

import vm.types.Type;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class ContractInstance implements Serializable {
    private final String id;
    private String currentState;
    private HashMap<String, Type> globalVariables;

    public ContractInstance(String initialState) {
        this.id = UUID.randomUUID().toString();
        this.currentState = initialState;
        this.globalVariables = new HashMap<>();
    }

    public String getId() {
        return id;
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

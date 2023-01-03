package messages;

import java.util.HashMap;

public class FunctionCallMessage extends Message {
    private final String contractId;
    private final String contractInstanceId;
    private final String function;
    private final HashMap<String, String> arguments;
    private final HashMap<String, PayToContract> assetArguments;

    public FunctionCallMessage(String contractId, String contractInstanceId, String function, HashMap<String, String> arguments) {
        this.contractId = contractId;
        this.contractInstanceId = contractInstanceId;
        this.function = function;
        this.arguments = arguments;
        this.assetArguments = null;
    }

    public FunctionCallMessage(String contractId, String contractInstanceId, String function, HashMap<String, String> arguments, HashMap<String, PayToContract> assetArguments) {
        this.contractId = contractId;
        this.contractInstanceId = contractInstanceId;
        this.function = function;
        this.arguments = arguments;
        this.assetArguments = assetArguments;
    }

    public String getContractId() {
        return contractId;
    }

    public String getContractInstanceId() {
        return contractInstanceId;
    }

    public String getFunction() {
        return function;
    }

    public HashMap<String, String> getArguments() {
        return arguments;
    }

    public HashMap<String, PayToContract> getAssetArguments() {
        return assetArguments;
    }
}

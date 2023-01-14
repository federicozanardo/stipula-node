package models.dto.requests.contract.function;

import models.dto.requests.Message;

import java.util.HashMap;

public class FunctionCallMessage extends Message {
    private final String contractId;
    private final String contractInstanceId;
    private final String function;
    private HashMap<String, String> arguments;
    private HashMap<String, PayToContract> assetArguments;

    public FunctionCallMessage(String contractId, String contractInstanceId, String function) {
        this.contractId = contractId;
        this.contractInstanceId = contractInstanceId;
        this.function = function;
        this.arguments = new HashMap<>();
        this.assetArguments = new HashMap<>();
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

    public void setArguments(HashMap<String, String> arguments) {
        this.arguments = arguments;
    }

    public void setAssetArguments(HashMap<String, PayToContract> assetArguments) {
        this.assetArguments = assetArguments;
    }
}

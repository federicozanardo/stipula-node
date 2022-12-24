package messages;

import types.address.Address;

import java.util.HashMap;

public class AgreementCallMessage extends Message {
    public String contractId;
    public HashMap<String, String> arguments;
    public HashMap<String, Address> parties;

    public AgreementCallMessage(String contractId, HashMap<String, String> arguments, HashMap<String, Address> parties) {
        this.contractId = contractId;
        this.arguments = arguments;
        this.parties = parties;
    }

    public String getContractId() {
        return contractId;
    }

    public HashMap<String, String> getArguments() {
        return arguments;
    }

    public HashMap<String, Address> getParties() {
        return parties;
    }

    @Override
    public String toString() {
        String str = "";

        str += contractId;

        for (HashMap.Entry<String, String> entry : this.arguments.entrySet()) {
            str += entry.getKey() + ": " + entry.getValue();
        }

        for (HashMap.Entry<String, Address> entry : this.parties.entrySet()) {
            str += entry.getKey() + ": " + entry.getValue().getPublicKey();
        }

        return str;
    }
}

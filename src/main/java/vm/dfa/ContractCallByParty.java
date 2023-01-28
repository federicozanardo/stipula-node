package vm.dfa;

import models.address.Address;

import java.util.ArrayList;

public class ContractCallByParty extends DfaState {
    private final ArrayList<Address> authorizedParties;

    public ContractCallByParty(String name, ArrayList<Address> authorizedParties) {
        super(name);
        this.authorizedParties = authorizedParties;
    }

    public ArrayList<Address> getAuthorizedParties() {
        return authorizedParties;
    }

    @Override
    public String toString() {
        return "ContractCallByParty{" +
                "authorizedParties=" + authorizedParties +
                '}';
    }
}

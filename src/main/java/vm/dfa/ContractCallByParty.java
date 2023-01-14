package vm.dfa;

import models.address.Address;

import java.util.ArrayList;

public class ContractCallByParty extends State {
    private final ArrayList<Address> authorizedParties;

    public ContractCallByParty(String name, ArrayList<Address> authorizedParties) {
        super(name);
        this.authorizedParties = authorizedParties;
    }

    public ArrayList<Address> getAuthorizedParties() {
        return authorizedParties;
    }
}

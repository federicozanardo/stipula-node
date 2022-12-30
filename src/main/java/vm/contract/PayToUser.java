package vm.contract;

import vm.types.address.Address;

public class PayToUser<T> {
    private Address user;
    private SingleUseSeal<T> singleUseSeal;
}

package vm.dfa;

public class ContractCallByEvent extends DfaState {
    private final String obligationFunctionName;

    public ContractCallByEvent(String name, String obligationFunctionName) {
        super(name);
        this.obligationFunctionName = obligationFunctionName;
    }

    public String getObligationFunctionName() {
        return obligationFunctionName;
    }

    @Override
    public String toString() {
        return "ContractCallByEvent{" +
                "obligationFunctionName='" + obligationFunctionName + '\'' +
                '}';
    }
}

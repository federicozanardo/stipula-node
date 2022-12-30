package vm.dfa;

public class ContractCallByEvent extends State {
    private final boolean isTransitionByEvent;

    public ContractCallByEvent(String name, boolean isTransitionByEvent) {
        super(name);
        this.isTransitionByEvent = isTransitionByEvent;
    }

    public boolean isTransitionByEvent() {
        return isTransitionByEvent;
    }
}

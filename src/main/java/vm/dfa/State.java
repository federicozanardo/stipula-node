package vm.dfa;

import java.io.Serializable;

public class State implements Serializable {
    private final String name;

    public State(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

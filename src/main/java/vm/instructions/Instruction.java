package vm.instructions;

import exceptions.trap.TrapException;
import vm.types.Type;

public abstract class Instruction {
    final private String name;

    public Instruction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract Type execute() throws TrapException;
}

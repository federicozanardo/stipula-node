package vm.storage;

import vm.types.TraceChange;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GlobalStorage {
    private final HashMap<String, TraceChange> storage;

    public GlobalStorage() {
        this.storage = new HashMap<>();
    }

    public HashMap<String, TraceChange> loadGlobalStorage() {
        // globalSpace.put("use_code", new TraceChange(new IntType(), true));
        return this.storage;
    }

    public void storeGlobalStorage(HashMap<String, TraceChange> updates) {
        if (this.storage.isEmpty()) {

        } else {
            for (HashMap.Entry<String, TraceChange> entry : this.storage.entrySet()) {
                TraceChange value = entry.getValue();
                System.out.println("storeGlobalSpace (globalSpace): " + entry.getKey() + ": " +
                        value.getValue().getValue() +
                        " (isChanged = " + value.isChanged() + ")");
            }

            // Hypothesis: length(keys(globalSpace)) < length(keys(updates))
            Set<String> difference = new HashSet<>(updates.keySet());
            difference.removeAll(this.storage.keySet());
            for (String missingKey : difference) {
                TraceChange value = updates.get(missingKey); //entry.getValue();
                System.out.println("storeGlobalSpace (updates): " + missingKey + ": " +
                        value.getValue().getValue() +
                        " (isChanged = " + value.isChanged() + ")");
            }
        }
    }
}

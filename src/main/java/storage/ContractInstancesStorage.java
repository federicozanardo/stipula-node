package storage;

import constants.Constants;
import models.contract.ContractInstance;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import vm.types.TraceChange;
import vm.types.Type;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.iq80.leveldb.impl.Iq80DBFactory.*;

public class ContractInstancesStorage extends Storage<ContractInstance> {
    public DB levelDb;
    private final HashMap<String, TraceChange> storage;

    public ContractInstancesStorage() throws IOException {
        // super(String.valueOf(Constants.CONTRACT_INSTANCES_PATH));
        this.storage = new HashMap<>();
    }

    public HashMap<String, TraceChange> getStorage() {
        return storage;
    }

    public void createContractInstance(ContractInstance instance) throws IOException {
        this.levelDb = factory.open(new File(String.valueOf(Constants.CONTRACT_INSTANCES_PATH)), new Options());
        levelDb.put(bytes(instance.getInstanceId()), this.serialize(instance));
        levelDb.close();
    }

    public ContractInstance getContractInstance(String contractInstanceId) throws IOException {
        this.levelDb = factory.open(new File(String.valueOf(Constants.CONTRACT_INSTANCES_PATH)), new Options());
        ContractInstance instance = this.deserialize(levelDb.get(bytes(contractInstanceId)));
        if (instance == null) {
            // Error: this contractInstanceId does not exist
            return null;
        }

        levelDb.close();
        return instance;
    }

    public void loadGlobalStorage(String contractInstanceId) throws IOException {
        this.levelDb = factory.open(new File(String.valueOf(Constants.CONTRACT_INSTANCES_PATH)), new Options());
        ContractInstance instance = this.deserialize(levelDb.get(bytes(contractInstanceId)));
        if (instance == null) {
            // Error: this contractInstanceId does not exist
            return;
        }

        for (HashMap.Entry<String, Type> entry : instance.getGlobalVariables().entrySet()) {
            System.out.println(asString(bytes(entry.getKey())) + ": " + entry.getValue().getValue());
            this.storage.put(entry.getKey(), new TraceChange(entry.getValue()));
        }

        levelDb.close();
    }

    /*public ContractInstance storeGlobalStorage(String contractId, HashMap<String, TraceChange> updates) throws IOException {
        // Create an instance of the current contract
        ContractInstance instance = new ContractInstance(contractId, "Inactive");

        for (HashMap.Entry<String, TraceChange> entry : updates.entrySet()) {
            Type value = entry.getValue().getValue();
            instance.getGlobalVariables().put(entry.getKey(), value);
        }

        // Store the instance
        levelDb.put(bytes(instance.getInstanceId()), this.serialize(instance));
        levelDb.close();

        return instance;
    }*/

    public void storeGlobalStorage(HashMap<String, TraceChange> updates, ContractInstance instance) throws IOException {
        this.levelDb = factory.open(new File(String.valueOf(Constants.CONTRACT_INSTANCES_PATH)), new Options());

        System.out.println("storeGlobalSpace: " + instance.getGlobalVariables());

        for (HashMap.Entry<String, TraceChange> entry : this.storage.entrySet()) {
            TraceChange value = entry.getValue();
            if (value.isChanged()) {
                instance.getGlobalVariables().put(entry.getKey(), value.getValue());
            }

            System.out.println("storeGlobalSpace (globalSpace): " + entry.getKey() + ": " +
                    value.getValue().getValue() +
                    " (isChanged = " + value.isChanged() + ")");
        }

        // Hypothesis: length(keys(globalSpace)) < length(keys(updates))
        Set<String> difference = new HashSet<>(updates.keySet());
        difference.removeAll(this.storage.keySet());
        for (String missingKey : difference) {
            TraceChange value = updates.get(missingKey);
            if (value.isChanged()) {
                instance.getGlobalVariables().put(missingKey, value.getValue());
            }

            System.out.println("storeGlobalSpace (updates): " + missingKey + ": " +
                    value.getValue().getValue() +
                    " (isChanged = " + value.isChanged() + ")");
        }

        System.out.println("storeGlobalSpace: " + instance.getGlobalVariables());
        System.out.println("storeGlobalSpace: " + instance.getInstanceId());

        levelDb.put(bytes(instance.getInstanceId()), this.serialize(instance));
        levelDb.close();
    }
}

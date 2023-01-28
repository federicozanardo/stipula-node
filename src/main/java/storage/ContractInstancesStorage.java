package storage;

import constants.Constants;
import models.contract.ContractInstance;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import vm.types.TraceChange;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class ContractInstancesStorage extends StorageSerializer<ContractInstance> {
    public DB levelDb;
    private final HashMap<String, TraceChange> storage;
    private final ReentrantLock mutex;

    public ContractInstancesStorage() {
        this.storage = new HashMap<>();
        this.mutex = new ReentrantLock();
    }

    public void createContractInstance(ContractInstance instance) throws IOException {
        mutex.lock();

        this.levelDb = factory.open(new File(String.valueOf(Constants.CONTRACT_INSTANCES_PATH)), new Options());
        levelDb.put(bytes(instance.getInstanceId()), this.serialize(instance));
        levelDb.close();

        mutex.unlock();
    }

    public ContractInstance getContractInstance(String contractInstanceId) throws IOException {
        mutex.lock();

        this.levelDb = factory.open(new File(String.valueOf(Constants.CONTRACT_INSTANCES_PATH)), new Options());
        ContractInstance instance = this.deserialize(levelDb.get(bytes(contractInstanceId)));
        if (instance == null) {
            // Error: this contractInstanceId does not exist
            mutex.unlock();
            return null;
        }

        levelDb.close();
        mutex.unlock();
        return instance;
    }

    public void storeGlobalStorage(HashMap<String, TraceChange> updates, ContractInstance instance) throws IOException {
        mutex.lock();

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
        mutex.unlock();
    }
}

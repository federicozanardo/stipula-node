package vm.storage;

import constants.Constants;
import vm.contract.ContractInstance;
import vm.types.TraceChange;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.iq80.leveldb.DB;

import org.iq80.leveldb.Options;
import vm.types.Type;

import static org.iq80.leveldb.impl.Iq80DBFactory.*;

public class GlobalStorage {
    private final HashMap<String, TraceChange> storage;
    private final DB levelDBStore;

    public GlobalStorage() throws IOException {
        this.storage = new HashMap<>();
        this.levelDBStore = factory.open(new File(String.valueOf(Constants.STORAGE_PATH)), new Options());
    }

    public HashMap<String, TraceChange> getStorage() {
        return storage;
    }

    public ContractInstance getContractInstance(String contractInstanceId) {
        ContractInstance instance = this.deserialize(levelDBStore.get(bytes(contractInstanceId)));
        if (instance == null) {
            // Error: this contractInstanceId does not exist
            return null;
        }
        return instance;
    }
    public void loadGlobalStorage(String contractInstanceId) {
        ContractInstance instance = this.deserialize(levelDBStore.get(bytes(contractInstanceId)));
        if (instance == null) {
            // Error: this contractInstanceId does not exist
            return;
        }

        for (HashMap.Entry<String, Type> entry : instance.getGlobalVariables().entrySet()) {
            System.out.println(asString(bytes(entry.getKey())) + ": " + entry.getValue().getValue());
            this.storage.put(entry.getKey(), new TraceChange(entry.getValue()));
        }
    }

    public ContractInstance storeGlobalStorage(String contractId, HashMap<String, TraceChange> updates) throws IOException {
        // Create an instance of the current contract
        ContractInstance instance = new ContractInstance(contractId, "Inactive");

        for (HashMap.Entry<String, TraceChange> entry : updates.entrySet()) {
            Type value = entry.getValue().getValue();
            instance.getGlobalVariables().put(entry.getKey(), value);
        }

        // Store the instance
        levelDBStore.put(bytes(instance.getInstanceId()), this.serialize(instance));
        levelDBStore.close();

        return instance;
    }
    public void storeGlobalStorage(HashMap<String, TraceChange> updates, ContractInstance instance) throws IOException {
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
        levelDBStore.close();
    }

    private byte[] serialize(ContractInstance myObject) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(myObject);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        } return null;
    }

    private ContractInstance deserialize(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);

            return (ContractInstance) in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        } return null;
    }
}

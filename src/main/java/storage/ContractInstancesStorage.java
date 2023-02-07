package storage;

import constants.Constants;
import exceptions.storage.ContractInstanceNotFoundException;
import models.contract.ContractInstance;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import vm.types.TraceChange;
import vm.types.Type;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class ContractInstancesStorage extends StorageSerializer<ContractInstance> {
    public DB levelDb;
    private final ReentrantLock mutex;

    public ContractInstancesStorage() {
        this.mutex = new ReentrantLock();
    }

    /**
     * Store a new contract instance.
     *
     * @param contractInstance: the new instance of the contract to save in the storage.
     * @throws IOException: throws when an error occur while opening or closing the connection with the storage.
     */
    public String saveContractInstance(ContractInstance contractInstance) throws IOException {
        mutex.lock();
        levelDb = factory.open(new File(String.valueOf(Constants.CONTRACT_INSTANCES_PATH)), new Options());

        // TODO: check that the id is unique
        String contractInstanceId = UUID.randomUUID().toString();
        levelDb.put(bytes(contractInstanceId), this.serialize(contractInstance));

        levelDb.close();
        mutex.unlock();
        return contractInstanceId;
    }

    /**
     * Get the contract instance information, given a contract instance id.
     *
     * @param contractInstanceId: id of the contract instance to find in the storage.
     * @return the contract instance information.
     * @throws IOException: throws when an error occur while opening or closing the connection with the storage.
     */
    public ContractInstance getContractInstance(String contractInstanceId) throws IOException, ContractInstanceNotFoundException {
        mutex.lock();
        levelDb = factory.open(new File(String.valueOf(Constants.CONTRACT_INSTANCES_PATH)), new Options());

        ContractInstance contractInstance = this.deserialize(levelDb.get(bytes(contractInstanceId)));
        if (contractInstance == null) {
            levelDb.close();
            mutex.unlock();
            throw new ContractInstanceNotFoundException(contractInstanceId);
        }

        levelDb.close();
        mutex.unlock();
        return contractInstance;
    }

    /**
     * This method allows to store the global space in the storage.
     *
     * @param contractInstanceId: id of the contract instance in which store the new global space values.
     * @param updates:            new global space values to store.
     * @throws IOException: throws when an error occur while opening or closing the connection with the storage.
     */
    public void storeGlobalSpace(String contractInstanceId, HashMap<String, TraceChange> updates)
            throws IOException,
            ContractInstanceNotFoundException {
        mutex.lock();
        levelDb = factory.open(new File(String.valueOf(Constants.CONTRACT_INSTANCES_PATH)), new Options());

        ContractInstance contractInstance = this.deserialize(levelDb.get(bytes(contractInstanceId)));
        if (contractInstance == null) {
            levelDb.close();
            mutex.unlock();
            throw new ContractInstanceNotFoundException(contractInstanceId);
        }

        System.out.println("storeGlobalSpace: contractInstance => " + contractInstance.getGlobalSpace());
        System.out.println("storeGlobalSpace: => updates " + updates);

        for (HashMap.Entry<String, TraceChange> entry : updates.entrySet()) {
            String variableName = entry.getKey();
            TraceChange value = entry.getValue();

            if (contractInstance.getGlobalSpace().containsKey(variableName)) {
                System.out.println("storeGlobalSpace: This is a new variable to store\n" +
                        "variable name: " + variableName + "\n" +
                        "value: " + value + "\n");
            } else {
                if (value.isChanged()) {
                    Type currentValue = contractInstance.getGlobalSpace().get(variableName);
                    System.out.println("storeGlobalSpace: This variable has changed value\n" +
                            "variable name: " + variableName + "\n" +
                            "current value: " + currentValue + "\n" +
                            "new value: " + value.getValue() + "\n");
                    contractInstance.getGlobalSpace().put(variableName, value.getValue());
                }
            }
        }

        levelDb.put(bytes(contractInstanceId), this.serialize(contractInstance));

        levelDb.close();
        mutex.unlock();
    }

    /**
     * @param contractInstanceId
     * @param partyName
     * @param functionName
     * @param argumentsTypes
     * @throws IOException
     * @throws ContractInstanceNotFoundException
     */
    public void storeStateMachine(String contractInstanceId, String partyName, String functionName, ArrayList<String> argumentsTypes)
            throws IOException,
            ContractInstanceNotFoundException {
        mutex.lock();
        levelDb = factory.open(new File(String.valueOf(Constants.CONTRACT_INSTANCES_PATH)), new Options());

        ContractInstance contractInstance = this.deserialize(levelDb.get(bytes(contractInstanceId)));
        if (contractInstance == null) {
            levelDb.close();
            mutex.unlock();
            throw new ContractInstanceNotFoundException(contractInstanceId);
        }

        contractInstance.getStateMachine().nextState(partyName, functionName, argumentsTypes);
        levelDb.put(bytes(contractInstanceId), this.serialize(contractInstance));

        levelDb.close();
        mutex.unlock();
    }

    /**
     * @param contractInstanceId
     * @param obligationFunctionName
     * @throws IOException
     * @throws ContractInstanceNotFoundException
     */
    public void storeStateMachine(String contractInstanceId, String obligationFunctionName)
            throws IOException,
            ContractInstanceNotFoundException {
        mutex.lock();
        levelDb = factory.open(new File(String.valueOf(Constants.CONTRACT_INSTANCES_PATH)), new Options());

        ContractInstance contractInstance = this.deserialize(levelDb.get(bytes(contractInstanceId)));
        if (contractInstance == null) {
            levelDb.close();
            mutex.unlock();
            throw new ContractInstanceNotFoundException(contractInstanceId);
        }

        contractInstance.getStateMachine().nextState(obligationFunctionName);
        levelDb.put(bytes(contractInstanceId), this.serialize(contractInstance));

        levelDb.close();
        mutex.unlock();
    }
}

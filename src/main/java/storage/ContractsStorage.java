package storage;

import constants.Constants;
import models.contract.Contract;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class ContractsStorage extends StorageSerializer<Contract> {
    private DB levelDb;
    private final ReentrantLock mutex;

    public ContractsStorage() {
        this.mutex = new ReentrantLock();
    }

    public String addContract(Contract contract) throws IOException {
        mutex.lock();
        levelDb = factory.open(new File(String.valueOf(Constants.CONTRACTS_PATH)), new Options());

        // TODO: check that the id is unique
        String contractId = UUID.randomUUID().toString();
        levelDb.put(bytes(contractId), this.serialize(contract));

        levelDb.close();
        mutex.unlock();
        return contractId;
    }

    public Contract getContract(String contractId) throws IOException {
        mutex.lock();
        levelDb = factory.open(new File(String.valueOf(Constants.CONTRACTS_PATH)), new Options());

        Contract contract = this.deserialize(levelDb.get(bytes(contractId)));
        if (contract == null) {
            // Error: this contractInstanceId does not exist
            levelDb.close();
            mutex.unlock();
            return null;
        }

        levelDb.close();
        mutex.unlock();
        return contract;
    }
}

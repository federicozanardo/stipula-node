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
        this.mutex.lock();

        // TODO: check that the id is unique
        String contractId = UUID.randomUUID().toString();

        this.levelDb = factory.open(new File(String.valueOf(Constants.CONTRACTS_PATH)), new Options());
        this.levelDb.put(bytes(contractId), this.serialize(contract));
        this.levelDb.close();

        this.mutex.unlock();
        return contractId;
    }

    public Contract getContract(String contractId) throws IOException {
        this.mutex.lock();
        this.levelDb = factory.open(new File(String.valueOf(Constants.CONTRACTS_PATH)), new Options());

        Contract contract = this.deserialize(this.levelDb.get(bytes(contractId)));
        if (contract == null) {
            // Error: this contractInstanceId does not exist
            return null;
        }

        this.levelDb.close();
        this.mutex.unlock();
        return contract;
    }
}

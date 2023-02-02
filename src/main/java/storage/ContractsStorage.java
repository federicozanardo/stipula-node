package storage;

import constants.Constants;
import exceptions.storage.ContractNotFoundException;
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

    /**
     * Save a new contract in the storage.
     *
     * @param contract: data of the contract to store.
     * @return the id of the contract just saved.
     * @throws IOException: throws when an error occur while opening or closing the connection with the storage.
     */
    public String saveContract(Contract contract) throws IOException {
        mutex.lock();
        levelDb = factory.open(new File(String.valueOf(Constants.CONTRACTS_PATH)), new Options());

        // TODO: check that the id is unique
        String contractId = UUID.randomUUID().toString();
        levelDb.put(bytes(contractId), this.serialize(contract));

        levelDb.close();
        mutex.unlock();
        return contractId;
    }

    /**
     * Get the contract information, given a contract id.
     *
     * @param contractId: id of the contract to find in the storage.
     * @return the contract information.
     * @throws IOException: throws when an error occur while opening or closing the connection with the storage.
     * @throws ContractNotFoundException: throws when the contract id is not referred to any contract saved in the storage.
     */
    public Contract getContract(String contractId) throws IOException, ContractNotFoundException {
        mutex.lock();
        levelDb = factory.open(new File(String.valueOf(Constants.CONTRACTS_PATH)), new Options());

        Contract contract = this.deserialize(levelDb.get(bytes(contractId)));
        if (contract == null) {
            levelDb.close();
            mutex.unlock();
            throw new ContractNotFoundException(contractId);
        }

        levelDb.close();
        mutex.unlock();
        return contract;
    }
}

package storage;

import constants.Constants;
import models.contract.Contract;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class ContractsStorage extends Storage<Contract> {
    private DB levelDb;

    public ContractsStorage() throws IOException {
        // super(String.valueOf(Constants.CONTRACTS_PATH));
    }

    public Contract getContract(String contractId) throws IOException {
        this.levelDb = factory.open(new File(String.valueOf(Constants.CONTRACTS_PATH)), new Options());

        Contract contract = this.deserialize(levelDb.get(bytes(contractId)));
        if (contract == null) {
            // Error: this contractInstanceId does not exist
            return null;
        }

        levelDb.close();

        return contract;
    }

    public String addContract(Contract contract) throws IOException {
        String contractId = UUID.randomUUID().toString();

        this.levelDb = factory.open(new File(String.valueOf(Constants.CONTRACTS_PATH)), new Options());
        levelDb.put(bytes(contractId), this.serialize(contract));
        levelDb.close();

        return contractId;
    }
}

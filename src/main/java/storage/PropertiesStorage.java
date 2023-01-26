package storage;

import constants.Constants;
import models.contract.Property;
import models.contract.SingleUseSeal;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import vm.types.FloatType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class PropertiesStorage extends StorageSerializer<ArrayList<Property>> {
    private DB levelDb;
    private final ReentrantLock mutex;

    public PropertiesStorage() {
        this.mutex = new ReentrantLock();
    }

    public void seed() throws IOException {
        FloatType amount = new FloatType(1200, 2);
        SingleUseSeal singleUseSeal = new SingleUseSeal(
                "1a3e31ad-5032-484c-9cdd-f1ed3bd760ac",
                amount,
                "f3hVW1Amltnqe3KvOT00eT7AU23FAUKdgmCluZB+nss=" // borrowerAddress
        );

        String propertyId = UUID.randomUUID().toString();
        Property property = new Property(propertyId, singleUseSeal);
        this.addFund("f3hVW1Amltnqe3KvOT00eT7AU23FAUKdgmCluZB+nss=", property);

        System.out.println("seed: propertyId => " + propertyId);
    }

    public ArrayList<Property> getFunds(String address) throws IOException {
        this.mutex.lock();

        this.levelDb = factory.open(new File(String.valueOf(Constants.PROPERTIES_PATH)), new Options());
        ArrayList<Property> funds = this.deserialize(levelDb.get(bytes(address)));

        if (funds == null) {
            // Error: this contractInstanceId does not exist
            this.mutex.unlock();
            return null;
        }

        this.mutex.unlock();
        return funds;
    }

    public Property getFund(String address, String singleUseSealId) throws IOException {
        this.mutex.lock();

        this.levelDb = factory.open(new File(String.valueOf(Constants.PROPERTIES_PATH)), new Options());
        ArrayList<Property> funds = this.deserialize(levelDb.get(bytes(address)));

        if (funds == null) {
            // Error: this contractInstanceId does not exist
            this.mutex.unlock();
            return null;
        }

        int i = 0;
        boolean found = false;
        Property fund = null;

        while (i < funds.size() && !found) {
            Property currentFund = funds.get(i);

            if (currentFund.getId().equals(singleUseSealId)) {
                found = true;
                fund = currentFund;
            } else {
                i++;
            }
        }

        if (!found) {
            // Error
            this.mutex.unlock();
            return null;
        }

        this.mutex.unlock();
        return fund;
    }

    public void addFund(String address, Property fund) throws IOException {
        this.mutex.lock();

        this.levelDb = factory.open(new File(String.valueOf(Constants.PROPERTIES_PATH)), new Options());
        ArrayList<Property> funds = null;

        try {
            funds = this.deserialize(levelDb.get(bytes(address)));
        } catch (Exception exception) {
            System.out.println("addFund: This address does not have any asset saved in the storage");
        }

        if (funds == null) {
            funds = new ArrayList<>();
        }

        funds.add(fund);
        levelDb.put(bytes(address), this.serialize(funds));
        levelDb.close();
        this.mutex.unlock();
    }

    public void addFunds(HashMap<String, SingleUseSeal> funds) throws IOException {
        this.mutex.lock();
        this.levelDb = factory.open(new File(String.valueOf(Constants.PROPERTIES_PATH)), new Options());

        for (HashMap.Entry<String, SingleUseSeal> entry : funds.entrySet()) {
            String address = entry.getKey();
            ArrayList<Property> currentFunds = null;

            try {
                currentFunds = this.deserialize(levelDb.get(bytes(address)));
            } catch (Exception exception) {
                System.out.println("addFund: This address does not have any asset saved in the storage");
            }

            if (currentFunds == null) {
                currentFunds = new ArrayList<>();
            }

            // TODO: check that the id is unique
            String propertyId = UUID.randomUUID().toString();
            Property property = new Property(propertyId, entry.getValue());
            currentFunds.add(property);
            levelDb.put(bytes(address), this.serialize(currentFunds));
        }

        for (HashMap.Entry<String, SingleUseSeal> entry : funds.entrySet()) {
            ArrayList<Property> currentFunds = this.deserialize(levelDb.get(bytes(entry.getKey())));
            System.out.println("addFunds: currentFunds => " + currentFunds);
        }

        levelDb.close();
        this.mutex.unlock();
    }
}

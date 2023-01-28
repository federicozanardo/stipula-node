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
        mutex.lock();

        this.levelDb = factory.open(new File(String.valueOf(Constants.PROPERTIES_PATH)), new Options());
        ArrayList<Property> funds = this.deserialize(levelDb.get(bytes(address)));

        if (funds == null) {
            // Error: this contractInstanceId does not exist
            mutex.unlock();
            return null;
        }

        mutex.unlock();
        System.out.println("getFunds: mutex.isLocked() => " + mutex.isLocked());
        return funds;
    }

    public Property getFund(String address, String propertyId) throws IOException {
        mutex.lock();

        this.levelDb = factory.open(new File(String.valueOf(Constants.PROPERTIES_PATH)), new Options());
        ArrayList<Property> funds = this.deserialize(levelDb.get(bytes(address)));

        if (funds == null) {
            // Error: this contractInstanceId does not exist
            mutex.unlock();
            return null;
        }

        int i = 0;
        boolean found = false;
        Property fund = null;

        while (i < funds.size() && !found) {
            Property currentFund = funds.get(i);

            if (currentFund.getId().equals(propertyId)) {
                found = true;
                fund = currentFund;
            } else {
                i++;
            }
        }

        if (!found) {
            // Error
            mutex.unlock();
            return null;
        }

        levelDb.close();
        mutex.unlock();
        return fund;
    }

    public void addFund(String address, Property fund) throws IOException {
        mutex.lock();

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
        mutex.unlock();
    }

    public void addFunds(HashMap<String, SingleUseSeal> funds) throws IOException {
        mutex.lock();
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
        mutex.unlock();
    }

    public void makePropertySpent(
            String address,
            String propertyId,
            String contractInstanceId,
            String unlockScript
    ) throws Exception {
        mutex.lock();

        this.levelDb = factory.open(new File(String.valueOf(Constants.PROPERTIES_PATH)), new Options());
        ArrayList<Property> funds = this.deserialize(levelDb.get(bytes(address)));
        System.out.println("makePropertySpent: currentFunds => " + funds);

        if (funds == null) {
            // Error: this contractInstanceId does not exist
            levelDb.close();
            mutex.unlock();
            throw new Exception("Impossible to find the funds given the following address => " + address);
        }

        int i = 0;
        boolean found = false;

        while (i < funds.size() && !found) {
            Property currentFund = funds.get(i);

            if (currentFund.getId().equals(propertyId)) {
                found = true;
            } else {
                i++;
            }
        }

        if (!found) {
            // Error
            levelDb.close();
            mutex.unlock();
            throw new Exception("Impossible to find the funds given the following address => " + address +
                    " and the propertyId => " + propertyId);
        }

        // Update the property
        funds.get(i).setContractInstanceId(contractInstanceId);
        funds.get(i).setUnlockScript(unlockScript);

        // Save
        levelDb.put(bytes(address), this.serialize(funds));
        System.out.println("makePropertySpent: updatedFunds => " + funds);

        levelDb.close();
        mutex.unlock();
    }
}

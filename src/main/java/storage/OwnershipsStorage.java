package storage;

import constants.Constants;
import exceptions.storage.OwnershipNotFoundException;
import exceptions.storage.OwnershipsNotFoundException;
import models.contract.Ownership;
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

public class OwnershipsStorage extends StorageSerializer<ArrayList<Ownership>> {
    private DB levelDb;
    private final ReentrantLock mutex;

    public OwnershipsStorage() {
        this.mutex = new ReentrantLock();
    }

    public void seed() throws IOException {
        String assetId = "1a3e31ad-5032-484c-9cdd-f1ed3bd760ac";
        String borrowerAddress = "f3hVW1Amltnqe3KvOT00eT7AU23FAUKdgmCluZB+nss=";
        String ownershipId = "1ce080e5-8c81-48d1-b732-006fa1cc4e2e";
        FloatType amount = new FloatType(1200, 2);

        SingleUseSeal singleUseSeal = new SingleUseSeal(assetId, amount, borrowerAddress);
        Ownership ownership = new Ownership(ownershipId, singleUseSeal);

        levelDb = factory.open(new File(String.valueOf(Constants.OWNERSHIPS_PATH)), new Options());
        ArrayList<Ownership> funds = null;

        try {
            funds = this.deserialize(levelDb.get(bytes(borrowerAddress)));
        } catch (Exception exception) {
            System.out.println("seed: This address does not have any asset saved in the storage");
        }

        if (funds == null) {
            funds = new ArrayList<>();
        }

        funds.add(ownership);
        levelDb.put(bytes(borrowerAddress), this.serialize(funds));
        levelDb.close();

        System.out.println("seed: ownershipId => " + ownershipId);
    }

    /**
     * This method allows to get the funds associated to a given address.
     *
     * @param address: it is needed in order to search the funds associated.
     * @return the funds associated to the address.
     * @throws IOException:                throws when an error occur while opening or closing the connection with the storage.
     * @throws OwnershipsNotFoundException : throws when there are no funds associated to the given address.
     */
    public ArrayList<Ownership> getFunds(String address) throws IOException, OwnershipsNotFoundException {
        mutex.lock();
        levelDb = factory.open(new File(String.valueOf(Constants.OWNERSHIPS_PATH)), new Options());

        ArrayList<Ownership> funds = this.deserialize(levelDb.get(bytes(address)));
        if (funds == null) {
            levelDb.close();
            mutex.unlock();
            throw new OwnershipsNotFoundException(address);
        }

        levelDb.close();
        mutex.unlock();
        return funds;
    }

    /**
     * This method allows to get a specific ownership, given an address.
     *
     * @param address:    the address associated to the ownership to obtain.
     * @param ownershipId: the id of the specific ownership to obtain.
     * @return the fund associated to the address.
     * @throws IOException:                throws when an error occur while opening or closing the connection with the storage.
     * @throws OwnershipsNotFoundException : throws when there are no funds associated to the given address.
     * @throws OwnershipNotFoundException  :   throws when the ownership id is not referred to the given address or to any ownership saved in the storage.
     */
    public Ownership getFund(String address, String ownershipId)
            throws IOException,
            OwnershipsNotFoundException,
            OwnershipNotFoundException {
        mutex.lock();

        levelDb = factory.open(new File(String.valueOf(Constants.OWNERSHIPS_PATH)), new Options());
        ArrayList<Ownership> funds = this.deserialize(levelDb.get(bytes(address)));

        if (funds == null) {
            levelDb.close();
            mutex.unlock();
            throw new OwnershipsNotFoundException(address);
        }

        int i = 0;
        boolean found = false;
        Ownership fund = null;

        while (i < funds.size() && !found) {
            Ownership currentFund = funds.get(i);

            if (currentFund.getId().equals(ownershipId)) {
                found = true;
                fund = currentFund;
            } else {
                i++;
            }
        }

        if (!found) {
            levelDb.close();
            mutex.unlock();
            throw new OwnershipNotFoundException(address, ownershipId);
        }

        levelDb.close();
        mutex.unlock();
        return fund;
    }

    /**
     * This method allows to add new funds.
     *
     * @param funds: the funds to be stored.
     * @throws IOException: throws when an error occur while opening or closing the connection with the storage.
     */
    public void addFunds(HashMap<String, SingleUseSeal> funds) throws IOException {
        mutex.lock();
        levelDb = factory.open(new File(String.valueOf(Constants.OWNERSHIPS_PATH)), new Options());

        for (HashMap.Entry<String, SingleUseSeal> entry : funds.entrySet()) {
            String address = entry.getKey();
            ArrayList<Ownership> currentFunds;

            // Try to get the funds associate to the address
            currentFunds = this.deserialize(levelDb.get(bytes(address)));

            if (currentFunds == null) {
                System.out.println("addFund: This address does not have any asset saved in the storage");
                currentFunds = new ArrayList<>();
            }

            // TODO: check that the id is unique
            String ownershipId = UUID.randomUUID().toString();
            Ownership ownership = new Ownership(ownershipId, entry.getValue());
            currentFunds.add(ownership);
            levelDb.put(bytes(address), this.serialize(currentFunds));
        }

        levelDb.close();
        mutex.unlock();
    }

    /**
     * This method allows to make spent a ownership.
     *
     * @param address:            the address associated to the ownership to make spent.
     * @param ownershipId:        the id of the specific ownership to make spent.
     * @param contractInstanceId: id of the contract instance to find in the storage.
     * @param unlockScript:       the first part of the script that can prove the spendability of the ownership.
     * @throws IOException:                throws when an error occur while opening or closing the connection with the storage.
     * @throws OwnershipsNotFoundException : throws when there are no funds associated to the given address.
     * @throws OwnershipNotFoundException  :   throws when the ownership id is not referred to the given address or to any ownership saved in the storage.
     */
    public void makeOwnershipSpent(
            String address,
            String ownershipId,
            String contractInstanceId,
            String unlockScript
    ) throws IOException, OwnershipsNotFoundException, OwnershipNotFoundException {
        mutex.lock();

        levelDb = factory.open(new File(String.valueOf(Constants.OWNERSHIPS_PATH)), new Options());
        ArrayList<Ownership> funds = this.deserialize(levelDb.get(bytes(address)));

        if (funds == null) {
            levelDb.close();
            mutex.unlock();
            throw new OwnershipsNotFoundException(address);
        }

        int i = 0;
        boolean found = false;

        while (i < funds.size() && !found) {
            Ownership currentFund = funds.get(i);

            if (currentFund.getId().equals(ownershipId)) {
                found = true;
            } else {
                i++;
            }
        }

        if (!found) {
            levelDb.close();
            mutex.unlock();
            throw new OwnershipNotFoundException(address, ownershipId);
        }

        // Update the ownership
        funds.get(i).setContractInstanceId(contractInstanceId);
        funds.get(i).setUnlockScript(unlockScript);

        // Save
        levelDb.put(bytes(address), this.serialize(funds));
        levelDb.close();
        mutex.unlock();
    }
}

package storage;

import constants.Constants;
import exceptions.storage.AssetNotFoundException;
import models.assets.Asset;
import models.assets.FungibleAsset;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class AssetsStorage extends StorageSerializer<Asset> {
    private DB levelDb;
    private final ReentrantLock mutex;

    public AssetsStorage() {
        this.mutex = new ReentrantLock();
    }

    public void seed() throws IOException {
        FungibleAsset bitcoin = new FungibleAsset("Bitcoin", "BTC", 10000, 2);
        String assetId = "1a3e31ad-5032-484c-9cdd-f1ed3bd760ac";
        Asset asset = new Asset(assetId, bitcoin);

        levelDb = factory.open(new File(String.valueOf(Constants.ASSETS_PATH)), new Options());
        levelDb.put(bytes(assetId), this.serialize(asset));
        levelDb.close();

        System.out.println("seed: assetId => " + assetId);
    }

    /**
     * Get the asset information, given an asset id.
     *
     * @param assetId: id of the asset to find in the storage.
     * @return the asset information.
     * @throws IOException: throws when an error occur while opening or closing the connection with the storage.
     * @throws AssetNotFoundException: throws when the asset id is not referred to any asset saved in the storage.
     */
    public Asset getAsset(String assetId) throws IOException, AssetNotFoundException {
        mutex.lock();
        levelDb = factory.open(new File(String.valueOf(Constants.ASSETS_PATH)), new Options());

        Asset asset = this.deserialize(levelDb.get(bytes(assetId)));
        if (asset == null) {
            levelDb.close();
            mutex.unlock();
            throw new AssetNotFoundException(assetId);
        }

        levelDb.close();
        mutex.unlock();
        return asset;
    }
}

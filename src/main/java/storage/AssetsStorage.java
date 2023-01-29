package storage;

import constants.Constants;
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

    public Asset getAsset(String assetId) throws IOException {
        mutex.lock();
        levelDb = factory.open(new File(String.valueOf(Constants.ASSETS_PATH)), new Options());

        Asset asset = this.deserialize(levelDb.get(bytes(assetId)));
        if (asset == null) {
            // Error: this contractInstanceId does not exist
            levelDb.close();
            mutex.unlock();
            return null;
        }

        levelDb.close();
        mutex.unlock();
        return asset;
    }
}

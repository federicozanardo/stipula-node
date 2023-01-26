package storage;

import constants.Constants;
import models.assets.Asset;
import models.assets.AssetConfig;
import models.assets.FungibleAsset;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class AssetsStorage extends StorageSerializer<Asset> {
    private DB levelDb;
    private final ReentrantLock mutex;

    public AssetsStorage() {
        mutex = new ReentrantLock();
    }

    public void seed() throws IOException {
        FungibleAsset bitcoin = new FungibleAsset("Bitcoin", "BTC", 10000, 2);
        String assetId = this.addAsset(bitcoin);
        System.out.println("seed: assetId => " + assetId);
    }

    public String addAsset(AssetConfig assetConfig) throws IOException {
        mutex.lock();

        // TODO: check that the id is unique
        String assetId = UUID.randomUUID().toString();
        Asset asset = new Asset(assetId, assetConfig);

        this.levelDb = factory.open(new File(String.valueOf(Constants.ASSETS_PATH)), new Options());
        this.levelDb.put(bytes(assetId), this.serialize(asset));
        this.levelDb.close();

        mutex.unlock();
        return assetId;
    }

    public Asset getAsset(String assetId) throws IOException {
        mutex.lock();
        this.levelDb = factory.open(new File(String.valueOf(Constants.ASSETS_PATH)), new Options());

        Asset asset = this.deserialize(this.levelDb.get(bytes(assetId)));
        if (asset == null) {
            mutex.unlock();
            // Error: this contractInstanceId does not exist
            return null;
        }

        this.levelDb.close();
        mutex.unlock();
        return asset;
    }
}

package vm.storage;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.*;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class Storage<T> {
    // public final DB levelDb;

    public Storage() {}

    /*public Storage(String path) throws IOException {
        this.levelDb = factory.open(new File(path), new Options());
    }

    public void close() throws IOException {
        this.levelDb.close();
    }*/

    public byte[] serialize(T myObject) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(myObject);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return null;
    }

    public T deserialize(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);

            return (T) in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return null;
    }
}

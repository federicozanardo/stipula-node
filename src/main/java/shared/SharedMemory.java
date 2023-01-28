package shared;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class SharedMemory<T> {
    private final HashMap<String, T> memory;
    private final ReentrantLock mutex;

    public SharedMemory() {
        this.memory = new HashMap<>();
        this.mutex = new ReentrantLock();
    }

    public String allocate() { // TODO: call it "allocate"?
        mutex.lock();

        String key = this.generateKey();
        this.memory.put(key, null);

        mutex.unlock();
        return key;
    }

    public boolean containsKey(String key) {
        mutex.lock();
        boolean result = this.memory.containsKey(key);
        mutex.unlock();
        return result;
    }

    public T get(String key) {
        mutex.lock();

        if (!this.memory.containsKey(key)) {
            mutex.unlock();
            throw new Error();
        }
        T value = this.memory.get(key);

        mutex.unlock();
        return value;
    }

    public void set(String key, T value) {
        mutex.lock();

        if (!this.memory.containsKey(key)) {
            mutex.unlock();
            throw new Error();
        }

        this.memory.put(key, value);
        mutex.unlock();
    }

    public void delete(String key) {
        mutex.lock();

        if (!this.memory.containsKey(key)) {
            mutex.unlock();
            throw new Error();
        }

        this.memory.remove(key);
        mutex.unlock();
    }

    private String generateKey() {
        String keyName = UUID.randomUUID().toString();

        while (this.memory.containsKey(keyName)) {
            keyName = UUID.randomUUID().toString();
        }

        return keyName;
    }
}

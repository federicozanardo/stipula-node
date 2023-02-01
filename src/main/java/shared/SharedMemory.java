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
    
    public void notifyThread(Thread thread, String key, T data) throws Exception {
        if (thread == null) {
            throw new Exception("notifyThread: Missing thread");
        }

        if (key == null || key.trim().equals("")) {
            throw new Exception("notifyThread: Missing key");
        }

        if (data == null) {
            throw new Exception("notifyThread: Missing data");
        }

        if (memory.containsKey(key)) {
            // Update the memory
            this.set(key, data);

            // Notify the thread
            System.out.println("notifyThread: Notifying the thread " + thread.getName() + "...");
            synchronized (thread) {
                thread.notify();
            }
            System.out.println("notifyThread: Thread notified");
        } else {
            throw new Exception("notifyThread: The key '" + key + "' is missing in the shared memory");
        }
    }
}

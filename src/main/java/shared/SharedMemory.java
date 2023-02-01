package shared;

import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class SharedMemory<T> {
    private final HashMap<String, T> memory;
    private final ReentrantLock mutex;

    public SharedMemory() {
        this.memory = new HashMap<>();
        this.mutex = new ReentrantLock();
    }

    /**
     * @return
     */
    public String allocate() {
        mutex.lock();

        String key = this.generateKey();
        memory.put(key, null);

        mutex.unlock();
        return key;
    }

    /**
     * @param key
     * @return
     */
    public boolean containsKey(String key) {
        mutex.lock();
        boolean result = memory.containsKey(key);
        mutex.unlock();
        return result;
    }

    /**
     * @param key
     * @return
     */
    public T get(String key) {
        mutex.lock();

        if (!memory.containsKey(key)) {
            mutex.unlock();
            throw new MissingResourceException(
                    "There is no value for the key \"" + key + "\"",
                    SharedMemory.class.getSimpleName(),
                    key
            );
        }
        T value = memory.get(key);

        mutex.unlock();
        return value;
    }

    /**
     * @param key
     * @param value
     */
    public void set(String key, T value) {
        mutex.lock();

        if (!memory.containsKey(key)) {
            mutex.unlock();
            throw new MissingResourceException(
                    "There is no value for the key \"" + key + "\"",
                    SharedMemory.class.getSimpleName(),
                    key
            );
        }

        memory.put(key, value);
        mutex.unlock();
    }

    /**
     * @param key
     */
    public void delete(String key) {
        mutex.lock();

        if (!memory.containsKey(key)) {
            mutex.unlock();
            throw new MissingResourceException(
                    "There is no value for the key \"" + key + "\"",
                    SharedMemory.class.getSimpleName(),
                    key
            );
        }

        memory.remove(key);
        mutex.unlock();
    }

    /**
     * @return
     */
    private String generateKey() {
        String keyName = UUID.randomUUID().toString();

        while (memory.containsKey(keyName)) {
            keyName = UUID.randomUUID().toString();
        }

        return keyName;
    }

    /**
     * @param thread
     * @param key
     * @param data
     * @throws Exception
     */
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

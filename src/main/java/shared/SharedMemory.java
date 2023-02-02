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
     * This method allows to allocate a cell linked to a key.
     *
     * @return the key useful to access to the cell.
     */
    public String allocate() {
        mutex.lock();

        String key = this.generateKey();
        memory.put(key, null);

        mutex.unlock();
        return key;
    }

    /**
     * Generate a random key.
     *
     * @return a random string that represents a key in the memory.
     */
    private String generateKey() {
        String keyName = UUID.randomUUID().toString();

        while (memory.containsKey(keyName)) {
            keyName = UUID.randomUUID().toString();
        }

        return keyName;
    }

    /**
     * Deallocate a cell given a key.
     *
     * @param key: key that links to a specific cell.
     */
    public void deallocate(String key) {
        mutex.lock();

        if (!memory.containsKey(key)) {
            mutex.unlock();
            throw new MissingResourceException(
                    "There is no value for the key '" + key + "'",
                    SharedMemory.class.getSimpleName(),
                    key
            );
        }

        memory.remove(key);
        mutex.unlock();
    }

    /**
     * Get the content of a cell given the key.
     *
     * @param key: key that links to the cell.
     * @return the data contained in the cell linked by the key.
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
     * Write data in a cell, given a specific key, and notify a thread.
     *
     * @param thread: thread to notify.
     * @param key: key linked to the cell.
     * @param data: data to insert in the cell, given the key.
     * @throws Exception: - if the thread is null or
     *                    - if the key is null or empty or
     *                    - if data is null.
     * @throws MissingResourceException: if the key does not exist in the memory.
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

        mutex.lock();

        // Check if the key exists
        if (memory.containsKey(key)) {
            // Update the memory
            memory.put(key, data);
            mutex.unlock();

            // Notify the thread
            System.out.println("notifyThread: Notifying the thread " + thread.getName() + "...");
            synchronized (thread) {
                thread.notify();
            }
            System.out.println("notifyThread: Thread notified");
        } else {
            mutex.unlock();
            throw new MissingResourceException(
                    "The key '" + key + "' is missing in the shared memory",
                    SharedMemory.class.getSimpleName(),
                    key
            );
        }
    }
}

package storage;

import exceptions.queue.QueueOverflowException;
import exceptions.queue.QueueUnderflowException;
import lib.datastructures.Pair;
import lib.datastructures.Queue;

import java.util.concurrent.locks.ReentrantLock;

public class StorageRequestQueue {
    private final Queue<Pair<Thread, Pair<String, StorageRequest>>> requests;
    private final ReentrantLock mutex;

    public StorageRequestQueue() {
        this.requests = new Queue<>(100);
        this.mutex = new ReentrantLock();
    }

    /**
     * @param thread
     * @param request
     * @throws QueueOverflowException
     */
    public void enqueue(Thread thread, String threadNameToNotify, StorageRequest request) throws QueueOverflowException {
        this.mutex.lock();

        if (this.requests.isFull()) {
            this.mutex.unlock();
            throw new QueueOverflowException();
        }

        this.requests.enqueue(new Pair<>(thread, new Pair<>(threadNameToNotify, request)));
        this.mutex.unlock();
        System.out.println("StorageRequestQueue:enqueue => " + requests);
    }

    /**
     * @return
     * @throws QueueUnderflowException
     */
    public Pair<Thread, Pair<String, StorageRequest>> dequeue() throws QueueUnderflowException {
        Pair<Thread, Pair<String, StorageRequest>> request;
        this.mutex.lock();

        if (this.requests.isEmpty()) {
            this.mutex.unlock();
            throw new QueueUnderflowException();
        }

        request = this.requests.dequeue();
        this.mutex.unlock();
        System.out.println("StorageRequestQueue:dequeue => " + requests);
        return request;
    }

    @Override
    public String toString() {
        return "StorageRequestQueue{" +
                "requests=" + requests +
                ", mutex=" + mutex +
                '}';
    }
}

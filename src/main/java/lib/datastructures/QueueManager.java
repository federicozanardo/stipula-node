package lib.datastructures;

import exceptions.queue.QueueOverflowException;
import exceptions.queue.QueueUnderflowException;
import models.dto.requests.Message;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.requests.contract.function.FunctionCall;

import java.util.concurrent.locks.ReentrantLock;

public class QueueManager {
    private final Queue<Pair<String, Message>> functionCallRequests;
    // private final Queue<Pair<String, TriggerRequest>> triggerRequests;
    private final Queue<Pair<String, Message>> triggerRequests;
    private final ReentrantLock mutex;

    public QueueManager() {
        this.functionCallRequests = new Queue<>(100);
        this.triggerRequests = new Queue<>(100);
        this.mutex = new ReentrantLock();
    }

    /**
     * @param threadName
     * @param value
     * @throws QueueOverflowException
     */
    public void enqueue(String threadName, Message value) throws QueueOverflowException {
        if (value instanceof AgreementCall || value instanceof FunctionCall) {
            mutex.lock();
            if (this.functionCallRequests.isFull()) {
                mutex.unlock();
                throw new QueueOverflowException();
            }
            this.functionCallRequests.enqueue(new Pair<>(threadName, value));
            System.out.println("QueueManager:enqueue => " + this.functionCallRequests);
            mutex.unlock();
        } else {
            throw new Error();
        }
    }

    /**
     * @param threadName
     * @param value
     * @throws QueueOverflowException
     */
    public void enqueue(String threadName, TriggerRequest value) throws QueueOverflowException {
        mutex.lock();
        if (this.triggerRequests.isFull()) {
            // System.out.println("enqueue: trigger queue is full");
            mutex.unlock();
            throw new QueueOverflowException();
        }
        this.triggerRequests.enqueue(new Pair<>(threadName, value));
        mutex.unlock();
    }

    /**
     * @return
     * @throws QueueUnderflowException
     */
    public Pair<String, Message> dequeue() throws QueueUnderflowException {
        Pair<String, Message> request = null;

        mutex.lock();
        if (!this.triggerRequests.isEmpty()) {
            request = this.triggerRequests.dequeue();
            mutex.unlock();
            return request;
        }

        if (this.functionCallRequests.isEmpty()) {
            mutex.unlock();
            throw new QueueUnderflowException();
        }
        request = this.functionCallRequests.dequeue();
        mutex.unlock();
        return request;
    }

    @Override
    public String toString() {
        return "QueueManager{" +
                "functionCallRequests=" + functionCallRequests +
                ", triggerRequests=" + triggerRequests +
                ", mutex=" + mutex +
                '}';
    }
}

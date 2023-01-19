package vm;

import exceptions.queue.QueueOverflowException;
import exceptions.queue.QueueUnderflowException;
import lib.datastructures.Pair;
import lib.datastructures.Queue;
import models.dto.requests.Message;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.requests.contract.function.FunctionCall;
import models.dto.requests.event.EventTriggerSchedulingRequest;

import java.util.concurrent.locks.ReentrantLock;

public class RequestQueue {
    private final Queue<Pair<Thread, Pair<String, Message>>> functionCallRequests;
    private final Queue<Pair<Thread, Pair<String, Message>>> triggerRequests;
    private final ReentrantLock mutex;

    public RequestQueue() {
        this.functionCallRequests = new Queue<>(100);
        this.triggerRequests = new Queue<>(100);
        this.mutex = new ReentrantLock();
    }

    /**
     * @param thread
     * @param value
     * @throws QueueOverflowException
     */
    public void enqueue(Thread thread, String threadNameToNotify, Message value) throws QueueOverflowException {
        if (value instanceof AgreementCall || value instanceof FunctionCall) {
            this.mutex.lock();
            if (this.functionCallRequests.isFull()) {
                this.mutex.unlock();
                throw new QueueOverflowException();
            }
            this.functionCallRequests.enqueue(new Pair<>(thread, new Pair<>(threadNameToNotify, value)));
            this.mutex.unlock();
        } else {
            throw new Error();
        }
    }

    /**
     * @param value
     * @throws QueueOverflowException
     */
    public void enqueue(EventTriggerSchedulingRequest value) throws QueueOverflowException {
        this.mutex.lock();
        if (this.triggerRequests.isFull()) {
            this.mutex.unlock();
            throw new QueueOverflowException();
        }
        this.triggerRequests.enqueue(new Pair<>(null, new Pair<>(null, value)));
        this.mutex.unlock();
    }

    /**
     * @return
     * @throws QueueUnderflowException
     */
    public Pair<Thread, Pair<String, Message>> dequeue() throws QueueUnderflowException {
        Pair<Thread, Pair<String, Message>> request;
        this.mutex.lock();

        if (!this.triggerRequests.isEmpty()) {
            request = this.triggerRequests.dequeue();
            this.mutex.unlock();
            return request;
        }

        if (this.functionCallRequests.isEmpty()) {
            this.mutex.unlock();
            throw new QueueUnderflowException();
        }

        request = this.functionCallRequests.dequeue();
        this.mutex.unlock();
        return request;
    }

    @Override
    public String toString() {
        return "RequestQueue{" +
                "functionCallRequests=" + functionCallRequests +
                ", triggerRequests=" + triggerRequests +
                ", mutex=" + mutex +
                '}';
    }
}

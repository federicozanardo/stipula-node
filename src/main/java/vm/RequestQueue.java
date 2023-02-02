package vm;

import exceptions.message.MessageNotSupportedException;
import exceptions.queue.QueueOverflowException;
import exceptions.queue.QueueUnderflowException;
import lib.datastructures.Pair;
import lib.datastructures.Queue;
import models.dto.requests.Message;
import models.dto.requests.SignedMessage;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.requests.contract.function.FunctionCall;
import models.dto.requests.event.EventTriggerSchedulingRequest;

import java.util.concurrent.locks.ReentrantLock;

public class RequestQueue {
    private final Queue<Pair<Thread, Object>> functionCallRequests;
    private final Queue<Pair<Thread, Object>> triggerRequests;
    private final ReentrantLock mutex;

    public RequestQueue() {
        this.functionCallRequests = new Queue<>(100);
        this.triggerRequests = new Queue<>(100);
        this.mutex = new ReentrantLock();
    }

    /**
     * This method enqueue a request received from a client.
     *
     * @param thread
     * @param value
     * @throws QueueOverflowException
     */
    public void enqueue(Thread thread, SignedMessage value) throws QueueOverflowException, MessageNotSupportedException {
        Message message = value.getMessage();

        if (message instanceof AgreementCall || message instanceof FunctionCall) {
            mutex.lock();

            if (functionCallRequests.isFull()) {
                mutex.unlock();
                throw new QueueOverflowException();
            }

            functionCallRequests.enqueue(new Pair<>(thread, value));
            mutex.unlock();
        } else {
            throw new MessageNotSupportedException("The only messages supported are AgreementCall and FunctionCall");
        }
    }

    /**
     * @param value
     * @throws QueueOverflowException
     */
    public void enqueue(EventTriggerSchedulingRequest value) throws QueueOverflowException {
        mutex.lock();

        if (triggerRequests.isFull()) {
            mutex.unlock();
            throw new QueueOverflowException();
        }

        triggerRequests.enqueue(new Pair<>(null, value));
        mutex.unlock();
    }

    /**
     * This method dequeue a request. It gives priority to the event trigger requests.
     *
     * @return
     * @throws QueueUnderflowException
     */
    public Pair<Thread, Object> dequeue() throws QueueUnderflowException {
        Pair<Thread, Object> request;
        mutex.lock();

        if (!triggerRequests.isEmpty()) {
            request = triggerRequests.dequeue();
            mutex.unlock();
            return request;
        }

        if (functionCallRequests.isEmpty()) {
            mutex.unlock();
            throw new QueueUnderflowException();
        }

        request = functionCallRequests.dequeue();
        mutex.unlock();
        return request;
    }

    @Override
    public String toString() {
        return "RequestQueue{" +
                "functionCallRequests=" + functionCallRequests +
                ", triggerRequests=" + triggerRequests +
                '}';
    }
}

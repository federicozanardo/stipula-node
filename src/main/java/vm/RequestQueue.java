package vm;

import exceptions.datastructures.queue.QueueOverflowException;
import exceptions.datastructures.queue.QueueUnderflowException;
import exceptions.models.dto.requests.MessageNotSupportedException;
import lib.datastructures.Pair;
import lib.datastructures.Queue;
import models.dto.requests.Message;
import models.dto.requests.SignedMessage;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.requests.contract.function.FunctionCall;
import models.dto.requests.event.EventSchedulingRequest;
import vm.event.EventTrigger;

import java.util.concurrent.locks.ReentrantLock;

public class RequestQueue {
    private final Queue<Pair<Thread, Object>> functionCallRequests;
    private final Queue<Pair<Thread, Object>> obligationRequests;
    private final ReentrantLock mutex;

    public RequestQueue() {
        this.functionCallRequests = new Queue<>(100);
        this.obligationRequests = new Queue<>(100);
        this.mutex = new ReentrantLock();
    }

    /**
     * This method enqueue a request received from a client.
     *
     * @param thread:                           the thread that enqueued the request.
     * @param value:                            value to be enqueued.
     * @throws QueueOverflowException:          throws when the current value exceed the queue space limit.
     * @throws MessageNotSupportedException:    throws if the message is different from {@link AgreementCall} and {@link FunctionCall}.
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
     * This method enqueue a request received from a {@link EventTrigger}.
     *
     * @param value:                    value to be enqueued.
     * @throws QueueOverflowException:  throws when the current value exceed the queue space limit.
     */
    public void enqueue(EventSchedulingRequest value) throws QueueOverflowException {
        mutex.lock();

        if (obligationRequests.isFull()) {
            mutex.unlock();
            throw new QueueOverflowException();
        }

        obligationRequests.enqueue(new Pair<>(null, value));
        mutex.unlock();
    }

    /**
     * This method dequeue a request. It gives priority to the event trigger requests.
     *
     * @return the request dequeued from the queue.
     * @throws QueueUnderflowException: throws when someone tries to dequeue an empty queue.
     */
    public Pair<Thread, Object> dequeue() throws QueueUnderflowException {
        Pair<Thread, Object> request;
        mutex.lock();

        if (!obligationRequests.isEmpty()) {
            request = obligationRequests.dequeue();
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
                ", obligationRequests=" + obligationRequests +
                '}';
    }
}

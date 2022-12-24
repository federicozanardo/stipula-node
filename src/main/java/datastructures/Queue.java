package datastructures;

import exceptions.queue.QueueOverflowException;
import exceptions.queue.QueueUnderflowException;
import types.Type;

import java.util.ArrayList;

public class Queue<T extends Type> {
    /**
     * Data structure used to represent the stack.
     */
    ArrayList<T> Q;

    /**
     *
     */
    int length;

    /**
     *
     */
    int head = 0;

    /**
     *
     */
    int tail = 0;

    public Queue(int length) {
        this.length = length;
    }

    /**
     * @param value
     */
    public void enqueue(T value) throws QueueOverflowException {
        if (this.isFull()) {
            throw new QueueOverflowException();
        } else {
            Q.add(tail, value);
            if (tail == length) {
                tail = 1;
            } else {
                tail++;
            }
        }
    }

    /**
     * @return
     */
    public T dequeue() throws QueueUnderflowException {
        if (this.isEmpty()) {
            throw new QueueUnderflowException();
        } else {
            T value = Q.get(head);
            if (head == length) {
                head = 1;
            } else {
                head++;
            }
            return value;
        }
    }

    // Other methods

    /**
     * @return
     */
    public boolean isFull() {
        return head == tail;
    }

    /**
     * @return
     */
    public boolean isEmpty() {
        return head == (tail + 1) || (head == 1 && tail == length);
    }
}
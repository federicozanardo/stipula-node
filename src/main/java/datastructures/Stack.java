package datastructures;

import exceptions.stack.StackOverflowException;
import exceptions.stack.StackUnderflowException;
import types.Type;

import java.util.ArrayList;

public class Stack<T extends Type> {

    /**
     * Data structure used to represent the stack.
     */
    ArrayList<T> S;

    /**
     * Represents the most recently inserted element.
     */
    int top = -1;

    /**
     * Variable to store size of the stack.
     */
    int size;

    public Stack(int size) {
        this.size = size;
        this.S = new ArrayList<T>(size);
    }

    /**
     * Push an element into the stack.
     *
     * @param value
     * @throws StackOverflowException
     */
    public void push(T value) throws StackOverflowException {
        // Check if the stack is full
        // if (top + 1 == size) {
        if (this.isFull()) {
            throw new StackOverflowException();
        } else {
            // Increment top to go to the next position
            top++;

            // Over-writing existing element
            if (S.size() > top) {
                S.set(top, value);
            } else {
                // Creating new element
                S.add(value);
            }
        }
    }

    /**
     * Return the last element and remove it from the stack.
     *
     * @return
     */
    public T pop() throws StackUnderflowException {
        // Check if the stack is empty
        // if (top == -1) {
        if (this.isEmpty()) {
            throw new StackUnderflowException();
        } else {
            T value = S.get(top);

            // Delete the last element by decrementing the `top` value
            top--;
            return value;
        }
    }

    /**
     * Check if stack is empty or not.
     *
     * @return true, if the stack is empty;
     * false, otherwise.
     */
    public boolean isEmpty() {
        return top == -1;
    }

    // Other methods

    /**
     * Check if stack is full or not.
     *
     * @return true, if the stack is full;
     * false, otherwise.
     */
    public boolean isFull() {
        return top == size - 1;
    }

    /**
     * Print the stack.
     */
    public String toString() {
        String line = "";

        for (int i = 0; i < top; i++) {
            line += S.get(i).getValue() + "->";
        }

        line += String.valueOf(S.get(top).getValue());

        return line;
    }
}
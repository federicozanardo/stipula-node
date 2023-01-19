package storage;

import exceptions.queue.QueueUnderflowException;
import lib.datastructures.Pair;
import models.dto.responses.Response;
import models.dto.responses.SuccessDataResponse;
import shared.SharedMemory;

public class Storage extends Thread {
    private final StorageRequestQueue queue;
    private final SharedMemory<Response> sharedMemory;

    public Storage(StorageRequestQueue queue, SharedMemory<Response> sharedMemory) {
        super(Storage.class.getSimpleName());
        this.queue = queue;
        this.sharedMemory = sharedMemory;
    }

    @Override
    public void run() {
        Pair<Thread, Pair<String, StorageRequest>> request;
        Pair<String, StorageRequest> packet;
        Thread thread;
        String whereToNotify;
        StorageRequest storageRequest;

        while (true) {
            System.out.println("Storage: Ready to dequeue a value...");
            try {
                request = this.queue.dequeue();

                thread = request.getFirst();
                packet = request.getSecond();
                whereToNotify = packet.getFirst();
                storageRequest = packet.getSecond();

                if (whereToNotify != null && storageRequest != null) {
                    System.out.println("\tStorage: queue => " + queue);
                    System.out.println("\tStorage: request received => " + packet);

                    // TODO: Save the request in the storage

                    this.sharedMemory.set(
                            whereToNotify,
                            new SuccessDataResponse("ack from Storage")
                    );

                    System.out.println("Storage: Now I'll notify the thread " + thread.getName());
                    synchronized (thread) {
                        thread.notify();
                    }

                    System.out.println("Storage: Bye bye!");


                    /*if (this.sharedMemory.containsKey(pair.getFirst().getName())) {
                        this.sharedMemory.put(
                                pair.getFirst().getName(),
                                new SuccessDataResponse("ack from Storage")
                        );

                        System.out.println("Storage: Now I'll notify the thread " + pair.getFirst().getName());
                        synchronized (pair.getFirst()) {
                            pair.getFirst().notify();
                        }

                        System.out.println("Storage: Bye bye!");
                    } else {
                        System.out.println("Storage: Oh no! There is no reference in the shared space for this thread " + pair.getFirst().getName());
                    }*/
                }
            } catch (QueueUnderflowException e) {
                // throw new RuntimeException(e);
                try {
                    System.out.println("Storage: I'm waiting...");
                    synchronized (this) {
                        this.wait();
                    }
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}

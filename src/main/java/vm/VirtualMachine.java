package vm;

import exceptions.queue.QueueOverflowException;
import exceptions.queue.QueueUnderflowException;
import lib.datastructures.Pair;
import models.dto.requests.Message;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.responses.Response;
import models.dto.responses.SuccessDataResponse;
import shared.SharedMemory;
import storage.Storage;
import storage.StorageRequest;
import storage.StorageRequestQueue;

import java.io.IOException;
import java.util.HashMap;

public class VirtualMachine extends Thread {
    private final RequestQueue queue;
    private final StorageRequestQueue storageRequestQueue;
    private final Storage storage;
    private final SharedMemory<Response> sharedMemory;

    public VirtualMachine(
            RequestQueue queue,
            StorageRequestQueue storageRequestQueue,
            Storage storage,
            SharedMemory<Response> sharedMemory
    ) {
        super(VirtualMachine.class.getSimpleName());
        this.queue = queue;
        this.storageRequestQueue = storageRequestQueue;
        this.storage = storage;
        this.sharedMemory = sharedMemory;
    }

    @Override
    public void run() {
        Pair<Thread, Pair<String, Message>> request;
        Pair<String, Message> packet;
        Thread thread;
        String whereToNotify;
        Message message;

        while (true) {
            System.out.println("VirtualMachine: Ready to dequeue a value...");
            try {
                request = this.queue.dequeue();
                System.out.println("VirtualMachine: request => " + request);

                thread = request.getFirst();
                packet = request.getSecond();
                whereToNotify = packet.getFirst();
                message = packet.getSecond();

                if (message != null) {
                    if (message instanceof AgreementCall) {
                        AgreementCall agreementCall = (AgreementCall) message;
                        System.out.println("\tVirtualMachine: queue => " + queue);
                        System.out.println("\tVirtualMachine: request received => " + agreementCall);

                        // TODO: Call SmartContractVirtualMachine in order to execute the request

                        this.storageRequestQueue.enqueue(
                                this,
                                thread.getName(),
                                new StorageRequest()
                        );

                        synchronized (this.storage) {
                            this.storage.notify();
                        }

                        // Wait a notification from the storage thread
                        synchronized (this) {
                            this.wait();
                        }

                        System.out.println("VirtualMachine: Notified from the storage");

                        if (thread != null && whereToNotify != null) {
                            System.out.println("VirtualMachine: response from Storage " + this.sharedMemory.get(whereToNotify));
                            String text = ((SuccessDataResponse) this.sharedMemory.get(whereToNotify))
                                    .getData().toString();

                            this.sharedMemory.set(
                                    whereToNotify,
                                    new SuccessDataResponse(text + " and VirtualMachine")
                            );

                            System.out.println("VirtualMachine: Now I'll notify the thread " + thread.getName());
                            synchronized (thread) {
                                thread.notify();
                            }
                        }

                        System.out.println("VirtualMachine: Bye bye!");

                        /*if (this.responsesToSend.containsKey(pair.getFirst().getName())) {
                            this.responsesToSend.put(
                                    pair.getFirst().getName(),
                                    new SuccessDataResponse("ack from VirtualMachine")
                            );

                            System.out.println("VirtualMachine: Now I'll notify the thread " + pair.getFirst().getName());
                            synchronized (pair.getFirst()) {
                                pair.getFirst().notify();
                            }

                            System.out.println("VirtualMachine: Bye bye!");
                        } else {
                            System.out.println("VirtualMachine: Oh no! There is no reference in the shared space for this thread " + pair.getFirst().getName());
                        }*/
                    }
                }
            } catch (QueueUnderflowException | QueueOverflowException | InterruptedException error) {
                // throw new RuntimeException(e);
                try {
                    System.out.println("VirtualMachine: I'm waiting...");
                    synchronized (this) {
                        this.wait();
                    }
                } catch (InterruptedException ex) {
                    System.out.println("VirtualMachine: " + ex);
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}

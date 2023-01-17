package vm;

import exceptions.queue.QueueUnderflowException;
import lib.datastructures.Pair;
import lib.datastructures.RequestQueue;
import models.dto.requests.Message;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.responses.Response;
import models.dto.responses.SuccessDataResponse;

import java.util.HashMap;

public class VirtualMachine extends Thread {
    private final RequestQueue queue;
    private final HashMap<String, Response> responsesToSend;

    public VirtualMachine(RequestQueue queue, HashMap<String, Response> responsesToSend) {
        super(VirtualMachine.class.getSimpleName());
        this.queue = queue;
        this.responsesToSend = responsesToSend;
    }

    @Override
    public void run() {
        Pair<Thread, Message> pair;
        Message message;

        while (true) {
            System.out.println("VirtualMachine: Ready to dequeue a value...");
            try {
                pair = this.queue.dequeue();
                message = pair.getSecond();

                if (message != null) {
                    if (message instanceof AgreementCall) {
                        AgreementCall agreementCall = (AgreementCall) message;
                        System.out.println("\tVirtualMachine: queue => " + queue);
                        System.out.println("\tVirtualMachine: request received => " + agreementCall);

                        // TODO: Call SmartContractVirtualMachine in order to execute the request

                        if (this.responsesToSend.containsKey(pair.getFirst().getName())) {
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
                            System.out.println("VirtualMachine: Oh no! There is no reference in the common space for this thread " + pair.getFirst().getName());
                        }
                    }
                }
            } catch (QueueUnderflowException e) {
                // throw new RuntimeException(e);
                try {
                    System.out.println("VirtualMachine: I'm waiting...");
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

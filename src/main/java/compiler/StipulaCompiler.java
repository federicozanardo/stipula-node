package compiler;

import event.EventTriggerHandler;
import models.dto.requests.event.EventTriggerRequest;
import models.dto.requests.event.EventTriggerSchedulingRequest;
import models.dto.responses.Response;
import models.dto.responses.SuccessDataResponse;

import java.util.HashMap;

public class StipulaCompiler implements Runnable {
    private final Thread clientHandler;
    // TODO: include kinda 'DeployContract' request
    private final EventTriggerHandler eventTriggerHandler;
    private final HashMap<String, Response> commonSpace;

    public StipulaCompiler(Thread clientHandler, EventTriggerHandler eventTriggerHandler, HashMap<String, Response> commonSpace) {
        this.clientHandler = clientHandler;
        this.eventTriggerHandler = eventTriggerHandler;
        this.commonSpace = commonSpace;
    }

    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            EventTriggerRequest request = new EventTriggerRequest(
                    "a" + i,
                    "b" + i,
                    "call" + i
            );
            EventTriggerSchedulingRequest schedulingRequest = new EventTriggerSchedulingRequest(request, 5);
            this.eventTriggerHandler.addTask(schedulingRequest);
            System.out.println("StipulaCompiler: added " + schedulingRequest);
        }

        if (this.commonSpace.containsKey(this.clientHandler.getName())) {
            this.commonSpace.put(this.clientHandler.getName(), new SuccessDataResponse("ack from StipulaCompiler thread"));

            System.out.println("StipulaCompiler: Now I'll notify the thread " + this.clientHandler.getName());
            synchronized (this.clientHandler) {
                this.clientHandler.notify();
            }

            System.out.println("StipulaCompiler: Bye bye!");
        } else {
            System.out.println("StipulaCompiler: Oh no! There is no reference in the common space for this thread " + this.clientHandler.getName());
        }
    }
}

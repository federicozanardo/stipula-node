package compiler;

import event.EventTriggerHandler;
import models.dto.requests.event.EventTriggerRequest;
import models.dto.requests.event.EventTriggerSchedulingRequest;

public class StipulaCompiler {
    private final EventTriggerHandler eventTriggerHandler;

    public StipulaCompiler(EventTriggerHandler eventTriggerHandler) {
        this.eventTriggerHandler = eventTriggerHandler;
    }

    public void compile() {
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
        }
    }
}

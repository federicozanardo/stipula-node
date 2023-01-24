import event.EventTriggerHandler;
import models.dto.responses.Response;
import server.MessageServer;
import shared.SharedMemory;
import storage.AssetsStorage;
import storage.ContractInstancesStorage;
import storage.ContractsStorage;
import vm.RequestQueue;
import vm.VirtualMachine;

import java.io.IOException;

class Main {
    public static void main(String[] args) {
        // Set up the requests queue
        RequestQueue requestQueue = new RequestQueue();

        SharedMemory<Response> sharedMemory = new SharedMemory<>();

        // Set up the storage
        ContractsStorage contractsStorage = new ContractsStorage();
        ContractInstancesStorage contractInstancesStorage = new ContractInstancesStorage();
        AssetsStorage assetsStorage = new AssetsStorage();
        /*try {
            assetsStorage.seed();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
        // assetId => 09c137f0-6ffc-425c-9657-de4577d8502c

        // TODO: asset transfers storage

        // Set up the virtual machine handler
        VirtualMachine virtualMachine = new VirtualMachine(
                requestQueue,
                sharedMemory,
                contractsStorage,
                contractInstancesStorage,
                assetsStorage
        );

        // Set up the Event trigger handler
        EventTriggerHandler eventTriggerHandler = new EventTriggerHandler(requestQueue, virtualMachine);

        // Set up the server
        Thread server = new Thread(
                new MessageServer(
                        61000,
                        requestQueue,
                        eventTriggerHandler,
                        virtualMachine,
                        sharedMemory,
                        contractsStorage),
                "Message server"
        );

        // Start the virtual machine
        virtualMachine.start();

        // Start the server
        server.start();
    }
}
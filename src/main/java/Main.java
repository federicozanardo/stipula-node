import event.EventTriggerHandler;
import models.dto.responses.Response;
import server.MessageServer;
import shared.SharedMemory;
import storage.PropertiesStorage;
import storage.AssetsStorage;
import storage.ContractInstancesStorage;
import storage.ContractsStorage;
import vm.RequestQueue;
import vm.VirtualMachine;

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
        PropertiesStorage propertiesStorage = new PropertiesStorage();
        /*try {
            assetTransfersStorage.seed();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
        // propertyId => 135a6e25-1d61-4a2c-99c5-2f1f756fe864

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
                        contractsStorage,
                        propertiesStorage
                ),
                "Message server"
        );

        // Start the virtual machine
        virtualMachine.start();

        // Start the server
        server.start();
    }
}
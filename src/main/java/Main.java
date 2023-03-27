import constants.Constants;
import models.dto.responses.Response;
import models.dto.responses.VirtualMachineResponse;
import server.MessageServer;
import shared.SharedMemory;
import storage.AssetsStorage;
import storage.ContractInstancesStorage;
import storage.ContractsStorage;
import storage.OwnershipsStorage;
import vm.RequestQueue;
import vm.VirtualMachine;
import vm.event.EventScheduler;

import java.io.File;
import java.io.IOException;

class Main {
    public static void main(String[] args) throws IOException {
        // Set up the requests queue
        RequestQueue requestQueue = new RequestQueue();

        // Set up a shared memory for client handlers and virtual machine
        SharedMemory<VirtualMachineResponse> sharedMemory = new SharedMemory<>();

        // Set up the storage
        ContractsStorage contractsStorage = new ContractsStorage();
        ContractInstancesStorage contractInstancesStorage = new ContractInstancesStorage();
        AssetsStorage assetsStorage = new AssetsStorage();
        OwnershipsStorage ownershipsStorage = new OwnershipsStorage();

        // Seeding the database
        setup(assetsStorage, ownershipsStorage);

        // Set up the Event trigger handler
        EventScheduler eventScheduler = new EventScheduler();

        // Set up the virtual machine
        VirtualMachine virtualMachine = new VirtualMachine(
                requestQueue,
                sharedMemory,
                eventScheduler,
                contractsStorage,
                contractInstancesStorage,
                assetsStorage,
                ownershipsStorage
        );

        // Set up the server
        MessageServer server = new MessageServer(
                61000,
                requestQueue,
                virtualMachine,
                sharedMemory,
                contractsStorage,
                ownershipsStorage,
                assetsStorage
        );

        // Start the virtual machine
        virtualMachine.start();

        // Start the server
        server.start();
    }

    private static void setup(
            AssetsStorage assetsStorage,
            OwnershipsStorage ownershipsStorage
    ) throws IOException {
        File storagePath = new File("storage");
        File contractStoragePath = new File(String.valueOf(Constants.CONTRACTS_PATH));
        File contractInstancesStoragePath = new File(String.valueOf(Constants.CONTRACT_INSTANCES_PATH));
        File assetsStoragePath = new File(String.valueOf(Constants.ASSETS_PATH));
        File ownershipsStoragePath = new File(String.valueOf(Constants.OWNERSHIPS_PATH));

        if (storagePath.exists()) {
            if (!storagePath.isDirectory()) {
                System.out.println("setup: storagePath is not a directory");
                System.exit(-1);
            } else {
                System.out.println("setup: storagePath exists");
            }
        } else {
            boolean result = storagePath.mkdir();
            if (result) {
                System.out.println("setup: storagePath created");
            } else {
                System.out.println("setup: error while creating storagePath");
                System.exit(-1);
            }
        }

        if (contractStoragePath.exists()) {
            if (!contractStoragePath.isDirectory()) {
                System.out.println("setup: contractStoragePath is not a directory");
                System.exit(-1);
            } else {
                System.out.println("setup: contractStoragePath exists");
            }
        } else {
            boolean result = contractStoragePath.mkdir();
            if (result) {
                System.out.println("setup: contractStoragePath created");
            } else {
                System.out.println("setup: error while creating contractStoragePath");
                System.exit(-1);
            }
        }

        if (contractInstancesStoragePath.exists()) {
            if (!contractInstancesStoragePath.isDirectory()) {
                System.out.println("setup: contractInstancesStoragePath is not a directory");
                System.exit(-1);
            } else {
                System.out.println("setup: contractInstancesStoragePath exists");
            }
        } else {
            boolean result = contractInstancesStoragePath.mkdir();
            if (result) {
                System.out.println("setup: contractInstancesStoragePath created");
            } else {
                System.out.println("setup: error while creating contractInstancesStoragePath");
                System.exit(-1);
            }
        }

        if (assetsStoragePath.exists()) {
            if (!assetsStoragePath.isDirectory()) {
                System.out.println("setup: assetsStoragePath is not a directory");
                System.exit(-1);
            } else {
                System.out.println("setup: assetsStoragePath exists");
            }
        } else {
            boolean result = assetsStoragePath.mkdir();
            if (result) {
                System.out.println("setup: assetsStoragePath created");
            } else {
                System.out.println("setup: error while creating assetsStoragePath");
                System.exit(-1);
            }
        }

        if (ownershipsStoragePath.exists()) {
            if (!ownershipsStoragePath.isDirectory()) {
                System.out.println("setup: ownershipsStoragePath is not a directory");
                System.exit(-1);
            } else {
                System.out.println("setup: ownershipsStoragePath exists");
            }
        } else {
            boolean result = ownershipsStoragePath.mkdir();
            if (result) {
                System.out.println("setup: ownershipsStoragePath created");
            } else {
                System.out.println("setup: error while creating ownershipsStoragePath");
                System.exit(-1);
            }
        }

        String value = System.getenv("SEED");
        System.out.println("setup: start seeding process? " + value);

        if (value.equals("yes")) {
            System.out.println("setup: seeding...");
            assetsStorage.seed();
            ownershipsStorage.seed();
        } else if (value.equals("no")) {
            System.out.println("setup: no need of seeding process");
        } else {
            System.out.println("setup: error, value not correct");
            System.exit(-1);
        }
    }
}
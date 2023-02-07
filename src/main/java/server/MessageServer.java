package server;

import models.dto.requests.MessageDeserializer;
import models.dto.requests.asset.GetAssetById;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.requests.contract.deploy.DeployContract;
import models.dto.requests.contract.function.FunctionCall;
import models.dto.requests.ownership.GetOwnershipsByAddress;
import models.dto.responses.Response;
import shared.SharedMemory;
import storage.ContractsStorage;
import storage.OwnershipsStorage;
import vm.RequestQueue;
import vm.VirtualMachine;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageServer extends Thread {
    private final int port;
    private final RequestQueue requestQueue;
    private final MessageDeserializer messageDeserializer;
    private final VirtualMachine virtualMachine;
    private final SharedMemory<Response> sharedMemory;
    private final ContractsStorage contractsStorage;
    private final OwnershipsStorage ownershipsStorage;

    public MessageServer(
            int port,
            RequestQueue requestQueue,
            VirtualMachine virtualMachine,
            SharedMemory<Response> sharedMemory,
            ContractsStorage contractsStorage,
            OwnershipsStorage ownershipsStorage
    ) {
        super(MessageServer.class.getSimpleName());
        this.port = port;
        this.requestQueue = requestQueue;
        this.virtualMachine = virtualMachine;
        this.sharedMemory = sharedMemory;
        this.contractsStorage = contractsStorage;
        this.ownershipsStorage = ownershipsStorage;

        // Set up the deserializer of messages
        this.messageDeserializer = new MessageDeserializer();

        // Asset
        this.messageDeserializer.registerDataType(GetAssetById.class.getSimpleName(), GetAssetById.class);

        // Contract
        this.messageDeserializer.registerDataType(AgreementCall.class.getSimpleName(), AgreementCall.class);
        this.messageDeserializer.registerDataType(DeployContract.class.getSimpleName(), DeployContract.class);
        this.messageDeserializer.registerDataType(FunctionCall.class.getSimpleName(), FunctionCall.class);

        // Ownership
        this.messageDeserializer.registerDataType(GetOwnershipsByAddress.class.getSimpleName(), GetOwnershipsByAddress.class);
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("MessageServer: Starting the server...");
            ServerSocket server = null;

            try {
                server = new ServerSocket(port);
            } catch (IOException exception) {
                System.out.println("MessageServer: Error while starting the server");
                System.out.println("MessageServer: " + exception);
            }

            if (server != null) {
                System.out.println("MessageServer: Server established");

                while (true) {
                    System.out.println("MessageServer: Ready to accept clients...");
                    Socket socket = null;

                    try {
                        socket = server.accept();
                    } catch (IOException exception) {
                        System.out.println("MessageServer: " + exception);
                        System.out.println("MessageServer: Closing server connection...");

                        try {
                            server.close();
                            System.out.println("MessageServer: Server connection closed");
                        } catch (IOException e) {
                            System.out.println("MessageServer: " + exception);
                            System.out.println("MessageServer: Error while closing server connection");
                            break;
                        }
                    }

                    if (socket != null && !server.isClosed()) {
                        System.out.println("MessageServer: New client. Delegating the client communication to a dedicated thread...");

                        // Allocate a cell in the shared memory
                        String threadName = sharedMemory.allocate();

                        // Create a new client handler
                        new ClientHandler(
                                threadName,
                                socket,
                                requestQueue,
                                virtualMachine,
                                sharedMemory,
                                contractsStorage,
                                ownershipsStorage,
                                messageDeserializer
                        ).start();

                        System.out.println("MessageServer: Client communication delegated");
                    }
                }
            }
        }
    }
}

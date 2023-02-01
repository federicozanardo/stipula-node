package server;

import models.dto.requests.MessageDeserializer;
import models.dto.requests.asset.GetAssetById;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.requests.contract.deploy.DeployContract;
import models.dto.requests.contract.function.FunctionCall;
import models.dto.requests.property.GetPropertiesByAddress;
import models.dto.responses.Response;
import shared.SharedMemory;
import storage.ContractsStorage;
import storage.PropertiesStorage;
import vm.RequestQueue;
import vm.VirtualMachine;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageServer implements Runnable {
    private final int port;
    private final RequestQueue requestQueue;
    private final MessageDeserializer messageDeserializer;
    private final VirtualMachine virtualMachine;
    private final SharedMemory<Response> sharedMemory;
    private final ContractsStorage contractsStorage;
    private final PropertiesStorage propertiesStorage;

    public MessageServer(
            int port,
            RequestQueue requestQueue,
            VirtualMachine virtualMachine,
            SharedMemory<Response> sharedMemory,
            ContractsStorage contractsStorage,
            PropertiesStorage propertiesStorage
    ) {
        this.port = port;
        this.requestQueue = requestQueue;
        this.virtualMachine = virtualMachine;
        this.sharedMemory = sharedMemory;
        this.contractsStorage = contractsStorage;
        this.propertiesStorage = propertiesStorage;

        // Set up the deserializer of messages
        this.messageDeserializer = new MessageDeserializer();

        // Asset
        this.messageDeserializer.registerDataType(GetAssetById.class.getSimpleName(), GetAssetById.class);

        // Contract
        this.messageDeserializer.registerDataType(AgreementCall.class.getSimpleName(), AgreementCall.class);
        this.messageDeserializer.registerDataType(DeployContract.class.getSimpleName(), DeployContract.class);
        this.messageDeserializer.registerDataType(FunctionCall.class.getSimpleName(), FunctionCall.class);

        // Property
        this.messageDeserializer.registerDataType(GetPropertiesByAddress.class.getSimpleName(), GetPropertiesByAddress.class);
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("MessageServer: Starting the server...");
            ServerSocket server = null;

            try {
                server = new ServerSocket(port);
            } catch (IOException error) {
                System.out.println("MessageServer: " + error);
            }

            if (server != null) {
                System.out.println("MessageServer: Server established");

                while (true) {
                    System.out.println("MessageServer: Ready to accept clients...");
                    Socket socket = null;

                    try {
                        socket = server.accept();
                    } catch (IOException error) {
                        System.out.println("MessageServer: " + error);
                        System.out.println("MessageServer: Closing server connection...");

                        try {
                            server.close();
                            System.out.println("MessageServer: Server connection closed");
                        } catch (IOException e) {
                            System.out.println("MessageServer: " + error);
                            System.out.println("MessageServer: Error while closing server connection");
                            break;
                        }
                    }

                    if (socket != null && !server.isClosed()) {
                        System.out.println("MessageServer: New client");
                        System.out.println("MessageServer: Delegating the client communication with a dedicated thread...");

                        String threadName = this.sharedMemory.allocate();
                        new ClientHandler(
                                threadName,
                                socket,
                                requestQueue,
                                virtualMachine,
                                sharedMemory,
                                contractsStorage,
                                propertiesStorage,
                                messageDeserializer
                        ).start();

                        System.out.println("MessageServer: Client communication delegated");
                    }
                }
            }
        }
    }
}

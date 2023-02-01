package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import compiler.StipulaCompiler;
import exceptions.message.MessageNotSupportedException;
import exceptions.queue.QueueOverflowException;
import models.contract.Property;
import models.dto.requests.Message;
import models.dto.requests.MessageDeserializer;
import models.dto.requests.SignedMessage;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.requests.contract.deploy.DeployContract;
import models.dto.requests.contract.function.FunctionCall;
import models.dto.requests.property.GetPropertiesByAddress;
import models.dto.responses.ErrorResponse;
import models.dto.responses.Response;
import models.dto.responses.SuccessDataResponse;
import shared.SharedMemory;
import storage.ContractsStorage;
import storage.PropertiesStorage;
import vm.RequestQueue;
import vm.VirtualMachine;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final RequestQueue requestQueue;
    private final VirtualMachine virtualMachine;
    private final SharedMemory<Response> sharedMemory;
    private final ContractsStorage contractsStorage;
    private final PropertiesStorage propertiesStorage;
    private final Gson gson;

    public ClientHandler(
            String name,
            Socket socket,
            RequestQueue requestQueue,
            VirtualMachine virtualMachine,
            SharedMemory<Response> sharedMemory,
            ContractsStorage contractsStorage,
            PropertiesStorage propertiesStorage,
            MessageDeserializer messageDeserializer
    ) {
        super(name);
        this.socket = socket;
        this.sharedMemory = sharedMemory;
        this.requestQueue = requestQueue;
        this.virtualMachine = virtualMachine;
        this.contractsStorage = contractsStorage;
        this.propertiesStorage = propertiesStorage;
        this.gson = new GsonBuilder().registerTypeAdapter(Message.class, messageDeserializer).create();
    }

    @Override
    public void run() {
        ClientConnection clientConnection;
        try {
            clientConnection = new ClientConnection(socket, gson);
        } catch (IOException e) {
            System.out.println("ClientHandler: Error while establishing the connection with the client");
            return;
        }

        try {
            System.out.println("ClientHandler: Client accepted " + this.getName());

            // Read the message from the client socket
            System.out.println("ClientHandler: Waiting for client input...");
            SignedMessage signedMessage;
            signedMessage = clientConnection.getInputMessage();

            if (signedMessage == null) {
                // TODO: Send a response to notify that the thread received an wrong request
                clientConnection.sendResponse(
                        new ErrorResponse(
                                123,
                                "Error while getting the message"
                        )
                );
                return;
            }

            // TODO: check signatures

            Message message = signedMessage.getMessage();

            if (message instanceof DeployContract) {
                // Set up the compiler
                StipulaCompiler compiler = new StipulaCompiler((DeployContract) message, contractsStorage);
                String contractId = compiler.compile();

                // Send the response
                Response response = new SuccessDataResponse(contractId);
                clientConnection.sendResponse(response);
            } else if (message instanceof GetPropertiesByAddress) {
                // Get all the properties associated to the address
                GetPropertiesByAddress getPropertiesByAddress = (GetPropertiesByAddress) message;
                ArrayList<Property> properties = propertiesStorage.getFunds(getPropertiesByAddress.getAddress());

                // Send the response
                Response response = new SuccessDataResponse(properties.toString());
                clientConnection.sendResponse(response);
            } else if (message instanceof AgreementCall || message instanceof FunctionCall) {
                try {
                    // Send a request to the queue manager
                    this.requestQueue.enqueue(
                            this,
                            this.getName(),
                            signedMessage
                    );

                    // Notify the virtual machine that a new request is ready to be fulfilled
                    synchronized (this.virtualMachine) {
                        this.virtualMachine.notify();
                    }

                    // Wait a notification from the virtual machine thread
                    synchronized (this) {
                        this.wait();
                    }

                    // Send the response
                    Response response = this.sharedMemory.get(Thread.currentThread().getName());
                    clientConnection.sendResponse(response);
                } catch (MessageNotSupportedException | QueueOverflowException exception) {
                    clientConnection.sendResponse(
                            new ErrorResponse(
                                    123,
                                    "Error while enqueuing the request"
                            )
                    );
                }
            } else {
                clientConnection.sendResponse(new ErrorResponse(123, "This is not a valid message"));
            }

            System.out.println("ClientHandler: Closing the connection with the client...");
            clientConnection.close();
            System.out.println("ClientHandler: Client connection closed");
        } catch (IOException | InterruptedException exception) {
            System.out.println("ClientHandler: " + exception);
        }
    }
}

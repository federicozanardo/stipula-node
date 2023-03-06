package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import compiler.Compiler;
import exceptions.datastructures.queue.QueueOverflowException;
import exceptions.models.dto.requests.MessageNotSupportedException;
import exceptions.storage.OwnershipsNotFoundException;
import models.contract.Ownership;
import models.dto.requests.Message;
import models.dto.requests.MessageDeserializer;
import models.dto.requests.SignedMessage;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.requests.contract.deploy.DeployContract;
import models.dto.requests.contract.function.FunctionCall;
import models.dto.requests.ownership.GetOwnershipsByAddress;
import models.dto.responses.ErrorResponse;
import models.dto.responses.Response;
import models.dto.responses.SuccessDataResponse;
import shared.SharedMemory;
import storage.ContractsStorage;
import storage.OwnershipsStorage;
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
    private final OwnershipsStorage ownershipsStorage;
    private final Gson gson;

    public ClientHandler(
            String name,
            Socket socket,
            RequestQueue requestQueue,
            VirtualMachine virtualMachine,
            SharedMemory<Response> sharedMemory,
            ContractsStorage contractsStorage,
            OwnershipsStorage ownershipsStorage,
            MessageDeserializer messageDeserializer
    ) {
        super(name);
        this.socket = socket;
        this.sharedMemory = sharedMemory;
        this.requestQueue = requestQueue;
        this.virtualMachine = virtualMachine;
        this.contractsStorage = contractsStorage;
        this.ownershipsStorage = ownershipsStorage;
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
            if (signedMessage.getSignatures().size() == 0) {
                // Error
            }

            if (signedMessage.getSignatures().size() > 1) {
                // Error
            }

            Message message = signedMessage.getMessage();

            if (message instanceof DeployContract) {
                // Set up the compiler
                Compiler compiler = new Compiler((DeployContract) message, contractsStorage);
                String contractId = compiler.compile();

                // Send the response
                Response response = new SuccessDataResponse(contractId);
                clientConnection.sendResponse(response);
            } else if (message instanceof GetOwnershipsByAddress) {
                // Get all the ownerships associated to the address
                GetOwnershipsByAddress getOwnershipsByAddress = (GetOwnershipsByAddress) message;
                String address = getOwnershipsByAddress.getAddress();
                Response response;

                try {
                    ArrayList<Ownership> ownerships = ownershipsStorage.getFunds(address);

                    // Prepare the response
                    response = new SuccessDataResponse(ownerships.toString());

                } catch (OwnershipsNotFoundException exception) {
                    // Prepare the response
                    response = new ErrorResponse(123, "There are no funds associated to the address = " + address);
                }

                // Send the response
                clientConnection.sendResponse(response);
            } else if (message instanceof AgreementCall || message instanceof FunctionCall) {
                try {
                    // Send a request to the queue manager
                    requestQueue.enqueue(this, signedMessage);

                    // Notify the virtual machine that a new request is ready to be fulfilled
                    synchronized (virtualMachine) {
                        virtualMachine.notify();
                    }

                    // Wait a notification from the virtual machine thread
                    synchronized (this) {
                        this.wait();
                    }

                    // Send the response
                    Response response = sharedMemory.get(Thread.currentThread().getName());
                    clientConnection.sendResponse(response);
                } catch (MessageNotSupportedException | QueueOverflowException exception) {
                    clientConnection.sendResponse(new ErrorResponse(123, "Error while enqueuing the request"));
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

        // Deallocate the cell from the shared memory
        sharedMemory.deallocate(this.getName());
    }
}

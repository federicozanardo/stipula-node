package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import compiler.StipulaCompiler;
import event.EventTriggerHandler;
import exceptions.queue.QueueOverflowException;
import models.contract.Property;
import models.dto.requests.Message;
import models.dto.requests.MessageDeserializer;
import models.dto.requests.SignedMessage;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.requests.contract.deploy.DeployContract;
import models.dto.requests.contract.function.FunctionCall;
import models.dto.requests.property.GetPropertiesByAddress;
import models.dto.responses.Response;
import models.dto.responses.SuccessDataResponse;
import shared.SharedMemory;
import storage.AssetTransfersStorage;
import storage.ContractsStorage;
import vm.RequestQueue;
import vm.VirtualMachine;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final RequestQueue requestQueue;
    private final EventTriggerHandler eventTriggerHandler;
    private final VirtualMachine virtualMachine;
    private final SharedMemory<Response> sharedMemory;
    private final ContractsStorage contractsStorage;
    private final AssetTransfersStorage assetTransfersStorage;
    private final Gson gson;

    public ClientHandler(
            String name,
            Socket socket,
            RequestQueue requestQueue,
            EventTriggerHandler eventTriggerHandler,
            VirtualMachine virtualMachine,
            SharedMemory<Response> sharedMemory,
            ContractsStorage contractsStorage,
            AssetTransfersStorage assetTransfersStorage,
            MessageDeserializer messageDeserializer
    ) {
        super(name);
        this.socket = socket;
        this.sharedMemory = sharedMemory;
        this.requestQueue = requestQueue;
        this.eventTriggerHandler = eventTriggerHandler;
        this.virtualMachine = virtualMachine;
        this.contractsStorage = contractsStorage;
        this.assetTransfersStorage = assetTransfersStorage;
        this.gson = new GsonBuilder().registerTypeAdapter(Message.class, messageDeserializer).create();
    }

    @Override
    public void run() {
        DataInputStream inputClientStream = null;
        DataOutputStream outputServerStream = null;

        try {
            // Take in input the client socket input stream
            inputClientStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            outputServerStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException error) {
            System.out.println("ClientHandler: " + error);
        }

        if (inputClientStream != null && outputServerStream != null) {
            try {
                System.out.println("ClientHandler: Client accepted " + this.getName());
                String json;

                // Read the message from the client socket
                System.out.println("ClientHandler: Waiting for client input...");
                SignedMessage signedMessage = null;
                try {
                    json = inputClientStream.readUTF();
                    signedMessage = gson.fromJson(json, SignedMessage.class);
                } catch (IOException error) {
                    System.out.println("ClientHandler: " + error);
                }

                if (signedMessage != null) {
                    System.out.println("ClientHandler: Delegate the request of the client to a dedicated thread. Waiting...");

                    Message message = signedMessage.getMessage();

                    if (message instanceof DeployContract) {
                        String threadName = this.sharedMemory.instantiate();
                        System.out.println("ClientHandler: threadName => " + threadName);

                        // Set up and start a compiler thread
                        new StipulaCompiler(
                                threadName,
                                this,
                                (DeployContract) message,
                                eventTriggerHandler,
                                sharedMemory,
                                contractsStorage
                        ).start();

                        // Wait a notification from the compiler thread
                        synchronized (this) {
                            this.wait();
                        }

                        System.out.println("ClientHandler: Notified from the compiler");

                        System.out.println("ClientHandler: Prepare the response...");
                        Response response = this.sharedMemory.get(Thread.currentThread().getName());
                        String jsonResponse = "";
                        if (response instanceof SuccessDataResponse) {
                            jsonResponse = gson.toJson(response, SuccessDataResponse.class);
                        }

                        System.out.println("ClientHandler: Sending response...");
                        try {
                            outputServerStream.writeUTF(jsonResponse);
                        } catch (IOException error) {
                            System.out.println("ClientHandler: " + error);
                        }
                    } else if (message instanceof GetPropertiesByAddress) {
                        GetPropertiesByAddress getPropertiesByAddress = (GetPropertiesByAddress) message;
                        ArrayList<Property> properties = assetTransfersStorage.getFunds(getPropertiesByAddress.getAddress());

                        System.out.println("ClientHandler: properties => " + properties);

                        System.out.println("ClientHandler: Prepare the response...");
                        Response response = new SuccessDataResponse(properties.toString());
                        String jsonResponse = "";
                        if (response instanceof SuccessDataResponse) {
                            jsonResponse = gson.toJson(response, SuccessDataResponse.class);
                        }

                        System.out.println("ClientHandler: Sending response...");
                        try {
                            outputServerStream.writeUTF(jsonResponse);
                        } catch (IOException error) {
                            System.out.println("ClientHandler: " + error);
                        }
                    } else if (message instanceof AgreementCall || message instanceof FunctionCall) {
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

                        System.out.println("ClientHandler: Prepare the response...");
                        Response response = this.sharedMemory.get(Thread.currentThread().getName());
                        String jsonResponse = "";
                        if (response instanceof SuccessDataResponse) {
                            jsonResponse = gson.toJson(response, SuccessDataResponse.class);
                        }

                        System.out.println("ClientHandler: Sending response...");
                        try {
                            outputServerStream.writeUTF(jsonResponse);
                        } catch (IOException error) {
                            System.out.println("ClientHandler: " + error);
                        }
                    } else {
                        // TODO: raise error. this is an invalid request.
                    }
                } else {
                    // TODO: Send a response to notify that the thread received an wrong request
                }

                System.out.println("ClientHandler: Response sent");
                System.out.println("ClientHandler: Closing the connection with the client...");
                socket.close();
                inputClientStream.close();
            } catch (IOException | InterruptedException | QueueOverflowException error) {
                System.out.println("ClientHandler: " + error);
            }
        } else {
            System.out.println("ClientHandler: Error while getting the streams from the client socket");
            System.out.println("ClientHandler: Closing the connection with the client...");

            try {
                socket.close();
                System.out.println("ClientHandler: Client connection closed");
            } catch (IOException error) {
                System.out.println("ClientHandler: " + error);
                System.out.println("ClientHandler: Error while closing the connection with the client");
            }
        }
    }
}

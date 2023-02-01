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
import models.dto.responses.Response;
import models.dto.responses.SuccessDataResponse;
import shared.SharedMemory;
import storage.ContractsStorage;
import storage.PropertiesStorage;
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
                        // Set up and start a compiler thread
                        StipulaCompiler compiler = new StipulaCompiler((DeployContract) message, contractsStorage);
                        String contractId = compiler.compile();

                        // Prepare the response
                        System.out.println("ClientHandler: Prepare the response...");
                        Response response = new SuccessDataResponse(contractId);
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
                        ArrayList<Property> properties = propertiesStorage.getFunds(getPropertiesByAddress.getAddress());

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
            } catch (MessageNotSupportedException e) {
                throw new RuntimeException(e);
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

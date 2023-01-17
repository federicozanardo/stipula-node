package server;

import lib.datastructures.RequestQueue;
import models.dto.requests.MessageDeserializer;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.requests.contract.function.FunctionCall;
import models.dto.responses.Response;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;

public class MessageServer implements Runnable {
    private final int port;
    private final HashMap<String, Response> responsesToSend;
    private final RequestQueue requestQueue;
    private final MessageDeserializer messageDeserializer;

    public MessageServer(int port, RequestQueue requestQueue) {
        this.port = port;
        this.requestQueue = requestQueue;
        this.responsesToSend = new HashMap<>();

        // Set up the deserializer of messages
        this.messageDeserializer = new MessageDeserializer();
        this.messageDeserializer.registerDataType(AgreementCall.class.getSimpleName(), AgreementCall.class);
        this.messageDeserializer.registerDataType(FunctionCall.class.getSimpleName(), FunctionCall.class);
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("MessageServer: Starting the server...");
            ServerSocket server = null;

            try {
                server = new ServerSocket(port);
            } catch (IOException error) {
                System.out.println("ClientHandler: " + error);
            }

            if (server != null) {
                System.out.println("MessageServer: Server established");

                while (true) {
                    System.out.println("MessageServer: Ready to accept clients...");
                    Socket socket = null;

                    try {
                        socket = server.accept();
                    } catch (IOException error) {
                        System.out.println("ClientHandler: " + error);
                        System.out.println("MessageServer: Closing server connection...");

                        try {
                            server.close();
                            System.out.println("MessageServer: Server connection closed");
                        } catch (IOException e) {
                            System.out.println("ClientHandler: " + error);
                            System.out.println("MessageServer: Error while closing server connection");
                            break;
                        }
                    }

                    if (socket != null && !server.isClosed()) {
                        System.out.println("MessageServer: New client");
                        System.out.println("MessageServer: Delegating the client communication with a dedicated thread...");

                        String threadName = this.generateThreadName();
                        this.responsesToSend.put(threadName, null);
                        new ClientHandler(threadName, socket, responsesToSend, requestQueue, messageDeserializer).start();

                        System.out.println("MessageServer: Client communication delegated");
                    }
                }
            }
        }
    }

    private String generateThreadName() {
        String threadName = UUID.randomUUID().toString();

        while (this.responsesToSend.containsKey(threadName)) {
            threadName = UUID.randomUUID().toString();
        }

        return threadName;
    }
}

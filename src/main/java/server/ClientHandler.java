package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import compiler.Compiler;
import exceptions.datastructures.queue.QueueOverflowException;
import exceptions.models.dto.requests.MessageNotSupportedException;
import exceptions.storage.OwnershipsNotFoundException;
import lib.crypto.Crypto;
import models.contract.Ownership;
import models.dto.requests.Message;
import models.dto.requests.MessageDeserializer;
import models.dto.requests.SignedMessage;
import models.dto.requests.contract.agreement.AgreementCall;
import models.dto.requests.contract.deploy.DeployContract;
import models.dto.requests.contract.function.FunctionCall;
import models.dto.requests.ownership.GetOwnershipsByAddress;
import models.dto.responses.VirtualMachineResponse;
import models.party.Party;
import shared.SharedMemory;
import storage.AssetsStorage;
import storage.ContractsStorage;
import storage.OwnershipsStorage;
import vm.RequestQueue;
import vm.VirtualMachine;

import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Map;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final RequestQueue requestQueue;
    private final VirtualMachine virtualMachine;
    private final SharedMemory<VirtualMachineResponse> sharedMemory;
    private final ContractsStorage contractsStorage;
    private final OwnershipsStorage ownershipsStorage;
    private final AssetsStorage assetsStorage;
    private final Gson gson;

    public ClientHandler(
            String name,
            Socket socket,
            RequestQueue requestQueue,
            VirtualMachine virtualMachine,
            SharedMemory<VirtualMachineResponse> sharedMemory,
            ContractsStorage contractsStorage,
            OwnershipsStorage ownershipsStorage,
            AssetsStorage assetsStorage,
            MessageDeserializer messageDeserializer
    ) {
        super(name);
        this.socket = socket;
        this.sharedMemory = sharedMemory;
        this.requestQueue = requestQueue;
        this.virtualMachine = virtualMachine;
        this.contractsStorage = contractsStorage;
        this.ownershipsStorage = ownershipsStorage;
        this.assetsStorage = assetsStorage;
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
                clientConnection.sendErrorResponse(300);
                return;
            }

            // Check message signatures
            if (signedMessage.getSignatures().size() == 0) {
                clientConnection.sendErrorResponse(301);
                return;
            }

            Message message = signedMessage.getMessage();

            if (message instanceof AgreementCall) {
                AgreementCall agreementCallMessage = (AgreementCall) message;

                if (signedMessage.getSignatures().size() < 2) {
                    clientConnection.sendErrorResponse(302);
                    return;
                } else {
                    if (agreementCallMessage.getParties().size() != signedMessage.getSignatures().size()) {
                        clientConnection.sendErrorResponse(303);
                        return;
                    } else {
                        for (Map.Entry<String, String> entrySignature : signedMessage.getSignatures().entrySet()) {
                            boolean found = false;
                            String pubKey = entrySignature.getKey();

                            for (Map.Entry<String, Party> entryParty : agreementCallMessage.getParties().entrySet()) {
                                if (entryParty.getValue().getPublicKey().equals(pubKey)) {
                                    found = true;
                                    // Check signature

                                    // Get public key
                                    PublicKey publicKey = Crypto.getPublicKeyFromString(pubKey);

                                    // Get signature
                                    String signature = signedMessage.getSignatures().get(pubKey);

                                    // Verify the signature
                                    boolean result = Crypto.verify(message.toString(), signature, publicKey);

                                    if (!result) {
                                        clientConnection.sendErrorResponse(304);
                                        return;
                                    }
                                }
                            }

                            if (!found) {
                                clientConnection.sendErrorResponse(305);
                                return;
                            }
                        }
                    }
                }
            } else {
                if (signedMessage.getSignatures().size() > 1) {
                    clientConnection.sendErrorResponse(306);
                    return;
                } else {
                    // Check signature
                    if (signedMessage.getSignatures().keySet().stream().findFirst().isPresent()) {
                        // Get public key
                        String pubKey = signedMessage.getSignatures().keySet().stream().findFirst().get();
                        PublicKey publicKey = Crypto.getPublicKeyFromString(pubKey);

                        // Get signature
                        String signature = signedMessage.getSignatures().get(pubKey);

                        // Verify the signature
                        boolean result = Crypto.verify(message.toString(), signature, publicKey);

                        if (!result) {
                            clientConnection.sendErrorResponse(307);
                            return;
                        }
                    }
                }
            }

            if (message instanceof DeployContract) {
                // Set up the compiler
                Compiler compiler = new Compiler((DeployContract) message, contractsStorage, assetsStorage);
                String contractId = compiler.compile();

                // Send the response
                clientConnection.sendSuccessDataResponse(200, contractId);
            } else if (message instanceof GetOwnershipsByAddress) {
                // Get all the ownerships associated to the address
                GetOwnershipsByAddress getOwnershipsByAddress = (GetOwnershipsByAddress) message;
                String address = getOwnershipsByAddress.getAddress();

                try {
                    ArrayList<Ownership> ownerships = ownershipsStorage.getFunds(address);

                    // Send the response
                    clientConnection.sendSuccessDataResponse(200, ownerships.toString());
                } catch (OwnershipsNotFoundException exception) {
                    // Send the response
                    clientConnection.sendErrorDataResponse(308, "There are no funds associated to the address = " + address);
                }
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
                    VirtualMachineResponse response = sharedMemory.get(Thread.currentThread().getName());
                    if (response.getStatusCode() >= 300) {
                        if (response.getData() != null) {
                            clientConnection.sendErrorResponse(response.getStatusCode());
                        } else {
                            clientConnection.sendErrorDataResponse(response.getStatusCode(), response.getData());
                        }
                    } else {
                        if (response.getData() != null) {
                            clientConnection.sendSuccessResponse(response.getStatusCode());
                        } else {
                            clientConnection.sendSuccessDataResponse(response.getStatusCode(), response.getData());
                        }
                    }
                } catch (MessageNotSupportedException | QueueOverflowException exception) {
                    clientConnection.sendErrorResponse(309);
                }
            } else {
                clientConnection.sendErrorResponse(310);
            }

            System.out.println("ClientHandler: Closing the connection with the client...");
            clientConnection.close();
            System.out.println("ClientHandler: Client connection closed");
        } catch (IOException | InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException |
                 SignatureException | InvalidKeyException exception) {
            System.out.println("ClientHandler: " + exception);
            exception.printStackTrace();
        }

        // Deallocate the cell from the shared memory
        sharedMemory.deallocate(this.getName());
    }
}

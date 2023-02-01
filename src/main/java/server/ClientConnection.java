package server;

import com.google.gson.Gson;
import models.dto.requests.SignedMessage;
import models.dto.responses.ErrorResponse;
import models.dto.responses.Response;
import models.dto.responses.SuccessDataResponse;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientConnection {
    private final Socket socket;
    private final DataInputStream inputClientStream;
    private final DataOutputStream outputServerStream;
    private final Gson gson;

    public ClientConnection(Socket socket, Gson gson) throws IOException {
        this.socket = socket;
        this.gson = gson;

        try {
            // Take in input the client socket input stream
            this.inputClientStream = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
            this.outputServerStream = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException exception) {
            System.out.println("ClientConnection: Error while getting the streams from the client socket");
            System.out.println("ClientConnection: " + exception);
            System.out.println("ClientConnection: Closing the connection with the client...");
            try {
                socket.close();
                System.out.println("ClientConnection: Client connection closed");
            } catch (IOException ex) {
                System.out.println("ClientConnection: Error while closing the connection with the client");
                System.out.println("ClientConnection: " + ex);
            }
            throw new IOException("Impossible to get the streams from the client socket");
        }
    }

    /**
     *
     * @return
     */
    public SignedMessage getInputMessage() {
        String json;
        SignedMessage signedMessage = null;

        try {
            json = inputClientStream.readUTF();
            signedMessage = gson.fromJson(json, SignedMessage.class);
        } catch (IOException error) {
            System.out.println("getInputMessage: Error while getting the input from the client");
            System.out.println("getInputMessage: " + error);
        }

        return signedMessage;
    }

    /**
     * @param response
     */
    public void sendResponse(Response response) {
        // Prepare the response
        String json = "";

        if (response instanceof SuccessDataResponse) {
            json = gson.toJson(response, SuccessDataResponse.class);
        } else if (response instanceof ErrorResponse) {
            json = gson.toJson(response, ErrorResponse.class);
        }

        System.out.println("sendResponse: Sending response...");
        try {
            outputServerStream.writeUTF(json);
        } catch (IOException error) {
            System.out.println("sendResponse: " + error);
        }
        System.out.println("sendResponse: Response sent");
    }

    /**
     * @throws IOException
     */
    public void close() throws IOException {
        inputClientStream.close();
        outputServerStream.close();
        socket.close();
    }
}

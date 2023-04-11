package server;

import com.google.gson.Gson;
import models.dto.requests.SignedMessage;
import models.dto.responses.Response;
import models.dto.responses.ResponseCodes;
import models.dto.responses.error.ErrorDataResponse;
import models.dto.responses.error.ErrorResponse;
import models.dto.responses.success.SuccessDataResponse;
import models.dto.responses.success.SuccessResponse;

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
     * Read the message from the socket.
     *
     * @return the SignedMessage received from the socket.
     */
    public SignedMessage getInputMessage() {
        String json;
        SignedMessage signedMessage = null;

        try {
            json = inputClientStream.readUTF();
            signedMessage = gson.fromJson(json, SignedMessage.class);
        } catch (IOException exception) {
            System.out.println("getInputMessage: Error while getting the input from the client");
            System.out.println("getInputMessage: " + exception);
        }

        return signedMessage;
    }

    /**
     * Send a success response without payload and with the default status code.
     */
    public void sendSuccessResponse() {
        SuccessResponse successResponse = new SuccessResponse();
        this.sendResponse(successResponse);
    }

    /**
     * Send a success response without payload and with a specific status code.
     *
     * @param statusCode: specific code to include in the response.
     */
    public void sendSuccessResponse(int statusCode) {
        SuccessResponse successResponse = new SuccessResponse(statusCode);
        this.sendResponse(successResponse);
    }

    /**
     * Send a success response with a custom payload and with default status code.
     *
     * @param data: payload to include in the response.
     */
    public void sendSuccessDataResponse(Object data) {
        SuccessDataResponse successDataResponse = new SuccessDataResponse(data);
        this.sendResponse(successDataResponse);
    }

    /**
     * Send a success response with a custom payload and with a specific status code.
     *
     * @param statusCode: specific code to include in the response.
     * @param data:       payload to include in the response.
     */
    public void sendSuccessDataResponse(int statusCode, Object data) {
        SuccessDataResponse successDataResponse = new SuccessDataResponse(statusCode, data);
        this.sendResponse(successDataResponse);
    }

    /**
     * Send an error response with a specific status code.
     *
     * @param statusCode: specific code to include in the response.
     */
    public void sendErrorResponse(int statusCode) {
        ErrorResponse errorResponse = new ErrorResponse(statusCode);
        this.sendResponse(errorResponse);
    }

    /**
     * Send an error response with a custom payload and with a specific status code.
     *
     * @param statusCode: specific code to include in the response.
     * @param data:       payload to include in the response.
     */
    public void sendErrorDataResponse(int statusCode, Object data) {
        String errorMessage = ResponseCodes.getMessage(statusCode);
        ErrorDataResponse errorDataResponse = new ErrorDataResponse(statusCode, errorMessage, data);
        this.sendResponse(errorDataResponse);
    }

    /**
     * Send a response to the client socket.
     *
     * @param response: object to send to the client socket.
     */
    private void sendResponse(Response response) {
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
        } catch (IOException exception) {
            System.out.println("sendResponse: " + exception);
        }
        System.out.println("sendResponse: Response sent");
    }

    /**
     * Close the socket with the client.
     *
     * @throws IOException: throws when an error occurred while trying to close the socket.
     */
    public void close() throws IOException {
        inputClientStream.close();
        outputServerStream.close();
        socket.close();
    }
}

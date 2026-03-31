package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private PrintWriter out;
    private BufferedReader in;

    // Constructor method
    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    // Method that listens for incoming messages from the client, and when a message
    // is received, it broadcasts the message to all clients through the server.
    @Override
    public void run() {
        try {
            // Sets up the input and output streams so the clients can communicate with the
            // server
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String encryptedMsg;
            // Listens for an incoming, encrypted message from the client
            // When the encrypted message is received, it is broadcasted to all of the
            // clients connected to the server
            while ((encryptedMsg = in.readLine()) != null) {
                server.broadcast(encryptedMsg, this);
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            // Removes the client from the server and closes their socket
            server.removeClient(this);
            close();
        }
    }

    // Method that sends the encrypted message to the client
    public void send(String encryptedMsg) {
        if (out != null) {
            out.println(encryptedMsg);
        }
    }

    // Method that closes the client's socket when disconnected from the server
    private void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing client socket.");
        }
    }
}

package src;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
    private static Server instance;
    private Map<String, Room> rooms = new HashMap<>();
    public static Server getInstance() {
        if (instance == null)
            instance = new Server();
        return instance;
    }

    private ServerSocket serverSocket;
    // List of all of the clients that are currently conncected to the server
    private final List<ClientHandler> clients = new ArrayList<>();
    // BufferedWriter to log all ENCRYPTED versions of the messages to chatlog.txt
    private BufferedWriter logWriter;

    // Method that starts the server on the specified port & accepts each incoming
    // client, creates a new ClientHandler for each client, and starts a new thread
    // for each client to listen for any incoming messages
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        logWriter = new BufferedWriter(new FileWriter("chatlog.txt", true));

        System.out.println("Server running on port " + port + "...");

        while (true) {
            // Accepts a new client connection
            Socket socket = serverSocket.accept();
            System.out.println("New client connected: " + socket.getInetAddress());

            // Creates a ClientHandler for the new client
            ClientHandler handler = new ClientHandler(socket, this);

            // Adds the new client to the list of currently connected clients
            synchronized (clients) {
                clients.add(handler);
            }

            // Starts a new thread for the new client
            Thread clientThread = new Thread(handler);
            clientThread.start();
        }
    }

    // Method that broadcasts the encrypted message to all of the clients that are
    // currently connected to the server, and logs the encrypted message to
    // chatlog.txt
    public void broadcast(String encryptedMsg, ClientHandler sender) {
        // Logs the encrypted message to chatlog.txt
        logMessage(encryptedMsg);

        // Sends the encrypted message to all of the clients currently connected to the
        // server
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.send(encryptedMsg);
            }
        }
    }

    // Method that logs the encrypted message to chatlog.txt, meaning that the
    // server never has access to the plaintext chat messages
    private void logMessage(String encryptedMsg) {
        try {
            logWriter.write(encryptedMsg);
            logWriter.newLine();
            logWriter.flush();
        } catch (IOException e) {
            System.out.println("Error writing to chatlog.txt");
        }
    }

    // Method that removes a client from the list of currently connected clients
    public void removeClient(ClientHandler handler) {
        synchronized (clients) {
            clients.remove(handler);
        }
        System.out.println("Client disconnected.");
    }

    // Main method that creates a new instance of the Server and starts the server
    // on port 1111
    public static void main(String[] args) {
        Server server = new Server();

        try {
            server.start(1111);
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

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
            String line;
            // Sets up the input and output streams so the clients can communicate with the
            // server
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Listens for an incoming, encrypted message from the client
            // When the encrypted message is received, it is broadcasted to all of the
            // clients connected to the server
            while ((line = in.readLine()) != null) {
                handleIncoming(line);
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            // Removes the client from the server and closes their socket
            server.removeClient(this);
            close();
        }
    }

    private void handleIncoming(String line) {
        try {
            if (line.startsWith("MSG|")) {
                handleMessage(line);
            } else if (line.startsWith("CREATE|")) {
                handleCreateRoom(line);
            } else if (line.startsWith("JOIN|")) {
                handleJoinRoom(line);
            } else if (line.startsWith("LEAVE|")) {
                handleLeaveRoom(line);
            } else if (line.startsWith("OPEN|")) {
                handleOpenRoom(line);
            } else {
                // fallback for old behavior
                server.broadcast(line, this);
            }
        } catch (Exception e) {
            sendToClient("ERROR|Malformed command");
        }
    }

    private void handleMessage(String line) {

    }

    private void handleCreateRoom(String line) {
        // Expected format: CREATE|RoomName
        String[] parts = line.split("\\|", 2);
        if (parts.length < 2) {
            sendToClient("ERROR|Invalid CREATE command format");
            return;
        }

        // Ask server
        String roomName = parts[1];
        // Ask server to create the room
        boolean created = server.createRoom(roomName);
        if (created) {
            sendToClient("ROOM_CREATED|" + roomName);
        } else {
            sendToClient("ERROR|Room already exists");
        }
        System.out.println("User " + username + " requested to create room: " + roomName);
    }

    private void handleJoinRoom(String line) {
        // Expected format: JOIN|RoomName
        String[] parts = line.split("\\|");
        if (parts.length != 2) {
            sendToClient("ERROR|Invalid JOIN command format");
            return;
        }
        String roomName = parts[1];
        // Ask server to add this user to the room
        boolean joined = server.joinRoom(roomName, this);
        if (joined) {
            sendToClient("JOINED|" + roomName);
            System.out.println("User " + username + " joined room: " + roomName);
        } else {
            sendToClient("ERROR|Room does not exist");
        }
    }

    private void handleLeaveRoom(String line) {
        // Expected format: MSG|RoomName|<encrypted_message>
        String[] parts = line.split("\\|", 3);
        if (parts.length != 3) {
            sendToClient("ERROR|Invalid LEAVE command format");
            return;
        }
        String roomName = parts[1].trim();
        String encryptedMessage = parts[2].trim();

        // Ask server to broadcast to the room
        boolean success = server.broadcastToRoom(roomName, encryptedMessage, this);
        if (!success) {
            sendToClient("ERROR|Room does not exist or you are not a member");
        }
    }

    private void handleOpenRoom(String line) { 

    }

    public void sendToClient(String msg) {
        out.println(msg);
    }

    public String getUsername() {
        return username;
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

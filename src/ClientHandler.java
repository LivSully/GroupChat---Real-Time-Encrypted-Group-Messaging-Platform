package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

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

    // Method that handles a command and registers the username of the client
    private void handleUser(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 2) {
            sendToClient("ERROR|Invalid USER format");
            return;
        }
        this.username = parts[1].trim();
        sendToClient("USERNAME_SET|" + username);
        System.out.println("User connected as: " + username);
    }

    // Method that continuously listens for incoming commands from the client
    @Override
    public void run() {
        try {
            String line;
            // Sets up the input and output streams so the clients can communicate with the server
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
            server.removeClient(this);
            close();
        }
    }

    // Method that handles incoming encrypted images and broadcasts them to the appropriate room members
    private void handleImage(String line) {
        String[] parts = line.split("\\|", 4);
        if (parts.length != 4) {
            sendToClient("ERROR|Invalid IMG format");
            return;
        }
        String roomName = parts[1].trim();
        String fileName = parts[2].trim();
        String encryptedImage = parts[3].trim();
        Room room = server.getRoom(roomName);
        if (room == null || !room.hasMember(this)) {
            sendToClient("ERROR|Room does not exist or you are not a member");
            return;
        }
        room.writeToLog("IMG|" + roomName + "|" + username + "|" + fileName + "|" + encryptedImage);
        for (ClientHandler ch : room.getMembers()) {
            if (ch != null && ch != this) {
                ch.sendToClient("IMG|" + roomName + "|" + username + "|" + fileName + "|" + encryptedImage);
            }
        }
    }

    // Method that handles INVITE|Room|TargetUsername commands
    private void handleInvite(String line) {
        // Expected format: INVITE|RoomName|TargetUsername
        String[] parts = line.split("\\|", 4);
        if (parts.length != 3) {
            sendToClient("ERROR|Invalid INVITE format");
            return;
        }
        String roomName = parts[1].trim();
        String targetUsername = parts[2].trim();

        Room room = server.getRoom(roomName);
        if (room == null || !room.hasMember(this)) {
            sendToClient("ERROR|You are not in room: " + roomName);
            return;
        }

        ClientHandler target = server.getClientByUsername(targetUsername);
        if (target == null) {
            sendToClient("ERROR|User not found: " + targetUsername);
            return;
        }

        server.joinRoom(roomName, target);
        target.sendToClient("INVITED|" + roomName);
        sendToClient("INVITE_SENT|" + targetUsername + "|" + roomName);
        System.out.println(username + " invited " + targetUsername + " to room: " + roomName);
    }

    // Method that handles all incoming commands and routes them to the appropriate handler method
    private void handleIncoming(String line) {
        try {
            if (username == null && !line.startsWith("USER|")) {
                sendToClient("ERROR|Authenticate first");
                return;
            }
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
            } else if (line.startsWith("USER|")) {
                handleUser(line);
            } else if (line.startsWith("IMG|")) {
                handleImage(line);
            } else if (line.startsWith("INVITE|")) {
                handleInvite(line);
            } else {
                // fallback for old behavior
                server.broadcast(line, this);
            }
        } catch (Exception e) {
            sendToClient("ERROR|Malformed command");
        }
    }

    // Method that handles MSG|Room|Timestamp|EncryptedMessage commands
    private void handleMessage(String line) {
        String[] parts = line.split("\\|", 4); 
        if (parts.length != 4) {
            sendToClient("ERROR|Invalid MSG format");
            return;
        }
        String roomName = parts[1].trim();
        String encryptedMessage = parts[3].trim(); 
        boolean success = server.broadcastToRoom(roomName, encryptedMessage, this);
        if (!success) {
            sendToClient("ERROR|Room does not exist or you are not a member");
        }
    }

    // Method that handles CREATE|Room commands
    private void handleCreateRoom(String line) {
        String[] parts = line.split("\\|", 2);
        if (parts.length < 2) {
            sendToClient("ERROR|Invalid CREATE command format");
            return;
        }
        String roomName = parts[1];
        boolean created = server.createRoom(roomName);
        if (created) {
            server.joinRoom(roomName, this);
            sendToClient("ROOM_CREATED|" + roomName);
        } else {
            sendToClient("ERROR|Room already exists");
        }
        System.out.println("User " + username + " requested to create room: " + roomName);
    }

    // Method that handles JOIN|Room commands
    private void handleJoinRoom(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 2) {
            sendToClient("ERROR|Invalid JOIN command format");
            return;
        }
        String roomName = parts[1];
        boolean joined = server.joinRoom(roomName, this);
        if (joined) {
            sendToClient("JOINED|" + roomName);
            System.out.println("User " + username + " joined room: " + roomName);
        } else {
            sendToClient("ERROR|Room does not exist");
        }
    }

    // Method that handles LEAVE|Room commands
    private void handleLeaveRoom(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 2) {
            sendToClient("ERROR|Invalid LEAVE command format");
            return;
        }
        String roomName = parts[1].trim();
        Room room = server.getRoom(roomName);
        if (room == null) {
            sendToClient("ERROR|Room does not exist");
            return;
        }
        room.removeMember(this);
        sendToClient("LEFT|" + roomName);
        System.out.println("User " + username + " left room: " + roomName);
    }

    // Method that handles OPEN|Room commands 
    private void handleOpenRoom(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 2) {
            sendToClient("ERROR|Invalid OPEN format");
            return;
        }
        String roomName = parts[1].trim();
        List<String> history = server.getRoomHistory(roomName);
        if (history == null) {
            sendToClient("ERROR|Room does not exist");
            return;
        }
        for (String encryptedMsg : history) {
            sendToClient("HISTORY|" + encryptedMsg);
        }
        sendToClient("HISTORY_END|" + roomName);
    }

    // Method that sends a message directly to the client
    public void sendToClient(String msg) {
        out.println(msg);
    }

    // Method that returns the username of the client
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

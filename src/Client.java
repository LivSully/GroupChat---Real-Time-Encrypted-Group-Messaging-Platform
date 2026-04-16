package src;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

import javax.swing.SwingUtilities;

public class Client {
    // Socket for connecting to server
    private Socket socket;
    // Output stream to send messages
    private PrintWriter out;
    // Input stream to receive messages
    private BufferedReader in;
    // GUI reference
    private ChatroomGUI gui;
    // Username of the user
    private String username;
    // Stores up to 10 joined room names
    private String[] joinedRooms;
    // Tracks which room is currently open/selected
    private int currentRoomIndex;
    // Server IP and port
    private static final String IP_ADDRESS = "10.2.130.202";
    private static final int PORT = 1111;

    // Constructor method that connects to the server and starts the client listener
    // thread
    public Client(String host, int port, ChatroomGUI gui, String username) throws IOException {
        this.gui = gui;
        this.username = username;
        this.joinedRooms = new String[10];
        this.currentRoomIndex = -1;
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // Tell server who this client is
        sendCommand("USER|" + username);
        // Start listening for server messages
        new Thread(new ClientListener()).start();
    }

    // Method that sends an encrypted image to the server
    public void sendImage(File file) {
        if (file == null)
            return;
        String currentRoom = getCurrentRoomName();
        if (currentRoom == null) {
            gui.appendMessage("No Room Selected!!");
            return;
        }
        try {
            // Read file into byte array
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            // Encrypt bytes
            byte[] encryptedBytes = AESUtil.encryptImage(fileBytes);
            // Convert to Base64 string so it can be sent as text
            String encoded = Base64.getEncoder().encodeToString(encryptedBytes);
            // Send command
            sendCommand(MessageFactory.buildImageMessage(currentRoom, file.getName(), encoded));
        } catch (Exception e) {
            gui.appendMessage("Error sending image.");
        }
    }

    // Method that sends a command to the server
    private void sendCommand(String command) {
        out.println(command);
    }

    // Method that adds a room to the client's room array
    public void addRoom(String roomName) {
        if (roomName == null || roomName.isBlank()) {
            return;
        }
        // prevent duplicates
        for (int i = 0; i < joinedRooms.length; i++) {
            if (roomName.equals(joinedRooms[i])) {
                return;
            }
        }
        for (int i = 0; i < joinedRooms.length; i++) {
            if (joinedRooms[i] == null) {
                joinedRooms[i] = roomName;
                // if no room is currently selected, select first added room
                if (currentRoomIndex == -1) {
                    currentRoomIndex = i;
                }
                SwingUtilities.invokeLater(() -> gui.refreshRoomList());
                return;
            }
        }
        gui.appendMessage("You cannot join more than 10 rooms.");
    }

    // Method that removes a room from the client's room array
    public void removeRoom(String roomName) {
        for (int i = 0; i < joinedRooms.length; i++) {
            if (roomName != null && roomName.equals(joinedRooms[i])) {
                joinedRooms[i] = null;
                if (currentRoomIndex == i) {
                    currentRoomIndex = -1;
                    // pick first available room if one exists
                    for (int j = 0; j < joinedRooms.length; j++) {
                        if (joinedRooms[j] != null) {
                            currentRoomIndex = j;
                            break;
                        }
                    }
                }
                SwingUtilities.invokeLater(() -> gui.refreshRoomList());
                return;
            }
        }
    }

    // Method that returns the array of joined rooms
    public String[] getJoinedRooms() {
        return joinedRooms;
    }

    // Method that sets the currently selected room based on sidebar's index
    public void selectRoomByIndex(int index) {
        if (index >= 0 && index < joinedRooms.length && joinedRooms[index] != null) {
            currentRoomIndex = index;
        }
    }

    // Method that returns the name of the currently selected room
    public String getCurrentRoomName() {
        if (currentRoomIndex == -1) {
            return null;
        }
        return joinedRooms[currentRoomIndex];
    }

    // Method that sends a create room command to the server
    public void createRoom(String roomName) {
        if (roomName == null || roomName.isBlank()) {
            gui.appendMessage("Room name cannot be empty.");
            return;
        }

        sendCommand("CREATE|" + roomName);
    }

    // Method that sends a join room command to the server
    public void joinRoom(String roomName) {
        if (roomName == null || roomName.isBlank()) {
            gui.appendMessage("Room name cannot be empty.");
            return;
        }

        sendCommand("JOIN|" + roomName);
    }

    // Method that sends a leave room command to the server
    public void leaveRoom(String roomName) {
        if (roomName == null || roomName.isBlank()) {
            gui.appendMessage("Room name cannot be empty.");
            return;
        }

        sendCommand("LEAVE|" + roomName);
    }

    // Method that sends an invite user command to the server
    public void inviteUser(String roomName, String targetUsername) {
        if (roomName == null || roomName.isBlank() || targetUsername == null || targetUsername.isBlank()) {
            gui.appendMessage("Room name and username cannot be empty.");
            return;
        }
        sendCommand("INVITE|" + roomName + "|" + targetUsername);
    }

    // Class to satisfy the MVC design principle by consistently building commands
    private static class MessageFactory {
        public static String buildTextMessage(String room, String timestamp, String encryptedText) {
            return "MSG|" + room + "|" + timestamp + "|" + encryptedText;
        }

        public static String buildImageMessage(String room, String fileName, String encodedEncryptedImage) {
            return "IMG|" + room + "|" + fileName + "|" + encodedEncryptedImage;
        }
    }

    // Method that sends an encrypted string message to the server
    public void sendMessage(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) {
            return;
        }

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm"));

        String currentRoom = getCurrentRoomName();
        if (currentRoom == null) {
            gui.appendMessage("No room selected.");
            return;
        }
        try {
            String encrypted = AESUtil.encrypt(plaintext);
            sendCommand(MessageFactory.buildTextMessage(currentRoom, timestamp, encrypted));
            gui.receiveMessage("[" + timestamp + "] " + username + ": " + plaintext);
        } catch (Exception e) {
            gui.appendMessage("Encryption error while sending message.");
        }
    }

    // Class that continuously listens for messages sent from the server and
    // processes them
    private class ClientListener implements Runnable {
        @Override
        public void run() {
            try {
                String encryptedMsg;

                while ((encryptedMsg = in.readLine()) != null) {
                    try {
                        handleServerMessage(encryptedMsg);
                    } catch (Exception e) {
                        gui.appendMessage("[Error decrypting message]");
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    gui.getStatusIndicator().setBackground(Color.RED);
                });

            } catch (IOException e) {
                gui.appendMessage("Disconnected from server.");
            }
        }
    }

    // Method that calls for the full room history from the server
    public void openRoom(String roomName) {
        if (roomName != null && !roomName.isBlank()) {
            sendCommand("OPEN|" + roomName);
        }
    }

    // Method that handles encrypted images and sends the decrypted image to the GUI
    private void handleIncomingImage(String msg) {
        String[] parts = msg.split("\\|", 5);
        if (parts.length != 5)
            return;
        String room = parts[1];
        String sender = parts[2];
        String fileName = parts[3];
        String encryptedPayload = parts[4];
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPayload);
            byte[] imageBytes = AESUtil.decryptImage(encryptedBytes);
            gui.receiveImage(room, sender, fileName, imageBytes);
        } catch (Exception e) {
            gui.appendMessage("[Error decrypting image]");
        }
    }

    // Method that handles various server messages based on the command prefix
    private void handleServerMessage(String msg) {

        if (msg.startsWith("USERNAME_SET|")) {
            gui.appendMessage("Connected as " + username);

        } else if (msg.startsWith("ROOM_CREATED|")) {
            String room = msg.split("\\|", 2)[1];
            addRoom(room);
            gui.appendMessage("Room created: " + room);

        } else if (msg.startsWith("JOINED|")) {
            String room = msg.split("\\|", 2)[1];
            addRoom(room);
            gui.appendMessage("Joined room: " + room);

        } else if (msg.startsWith("LEFT|")) {
            String room = msg.split("\\|", 2)[1];
            removeRoom(room);
            gui.appendMessage("Left room: " + room);

        } else if (msg.startsWith("INVITE_SENT|")) {
            String[] parts = msg.split("\\|", 3);
            gui.appendMessage("Invite sent to " + parts[1] + " for room: " + parts[2]);

        } else if (msg.startsWith("INVITED|")) {
            // Another user invited YOU to a room — join it
            String room = msg.split("\\|", 2)[1];
            joinRoom(room);
            gui.appendMessage("You were invited to room: " + room);

        } else if (msg.startsWith("IMG|")) {
            handleIncomingImage(msg);

        } else if (msg.startsWith("ERROR|")) {
            gui.appendMessage("[Server Error] " + msg.split("\\|", 2)[1]);

        } else if (msg.startsWith("MSG|")) {
            handleIncomingRoomMessage(msg);

        } else if (msg.startsWith("HISTORY|")) {
            handleIncomingHistory(msg);

        } else {
            gui.appendMessage(msg);
        }
    }

    // Method that handles encrypted messages and sends the decrypted message to the
    // GUI
    private void handleIncomingRoomMessage(String msg) {
        String[] parts = msg.split("\\|", 5);
        if (parts.length != 5)
            return;
        String room = parts[1];
        String sender = parts[2];
        String timestamp = parts[3];
        String encryptedPayload = parts[4];
        try {
            String plaintext = AESUtil.decrypt(encryptedPayload);
            gui.receiveMessage("[" + timestamp + "] " + sender + ": " + plaintext);
        } catch (Exception e) {
            gui.appendMessage("[Error decrypting message]");
        }
    }

    // Method that disconnects the client
    public void disconnect() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method that requests the chat history for a room from the server
    private void handleIncomingHistory(String msg) {
        String[] parts = msg.split("\\|", 2);
        if (parts.length < 2)
            return;

        String line = parts[1];

        if (line.startsWith("MSG|")) {
            String[] p = line.split("\\|", 5);
            if (p.length != 5)
                return;

            String sender = p[2];
            String timestamp = p[3];
            try {
                String plaintext = AESUtil.decrypt(p[4]);
                gui.receiveMessage("[" + timestamp + "] " + sender + ": " + plaintext);
            } catch (Exception e) {
                gui.appendMessage("[Error decrypting history message]");
            }

        } else if (line.startsWith("IMG|")) {
            String[] p = line.split("\\|", 5);
            if (p.length != 5)
                return;

            String room = p[1];
            String sender = p[2];
            String fileName = p[3];

            try {
                byte[] encryptedBytes = Base64.getDecoder().decode(p[4]);
                byte[] imageBytes = AESUtil.decryptImage(encryptedBytes);
                gui.receiveImage(room, sender, fileName, imageBytes);
            } catch (Exception e) {
                gui.appendMessage("[Error loading history image]");
            }
        }
    }

    // Main method
    public static void main(String[] args) {
        ChatroomGUI gui = new ChatroomGUI("User");
        try {
            gui.connect(IP_ADDRESS, PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
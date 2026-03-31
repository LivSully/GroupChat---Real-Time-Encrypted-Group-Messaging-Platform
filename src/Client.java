package src;
/*
This class represents the client side networking component
of the Secure Chat system! It handles connecting to the server,
sendign encrypted messages, recieving encrypted messages,
decrypting them for the user to read, and updating the GUI.

This class requires:
-ChatClientGUI (To display messages)
-AESUTIL (For all encryption and decryption)
-Server (For actual server connection)
*/

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.SwingUtilities;

public class Client {
    // Socket for connecting to server
    private Socket socket;
    // Output stream to send messages
    private PrintWriter out;
    // Input stream to receive messages
    private BufferedReader in;
    // GUI so messages will be displayed
    private ChatClientGUI gui;
    // Username of the user
    private String username;
    // Server IP address and port
    // NOTE: THESE MUST MATCH WHOEVER IS HOSTING SERVER
    private static final String IP_ADDRESS = "10.2.130.128";
    private static final int port = 1111;

    // Connect client to server and creates new thread to start listening
    public Client(String host, int port, ChatClientGUI gui, String username) throws IOException {
        this.gui = gui;
        this.username = username;
        // Establish Connection
        socket = new Socket(host, port);
        // Initialize in and out streams
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        new Thread(new ClientListener()).start();
    }

    // Formatting outgoing messages
    public class MessageFactory {
        // Username + (Message)
        public static String createFormattedMessage(String username, String message) {
            return username + ": " + message;
        }
    }

    // encrypts message with AES then sends message
    public void sendMessage(String plaintext) {
        try {
            // Format the mesage with username
            String fullMessage = MessageFactory.createFormattedMessage(username, plaintext);
            // Encrypts the message with AES
            String encrypted = AESUtil.encrypt(fullMessage);
            // Send message
            out.println(encrypted);
        } catch (Exception e) {
            gui.appendMessage("Encryption error");
        }
    }

    // Listener runs a separate thread and listens for any incoming message
    private class ClientListener implements Runnable {
        public void run() {
            try {
                String encryptedMsg;
                // Continuously listening for messge
                while ((encryptedMsg = in.readLine()) != null) {
                    try {
                        // Decrypt any received mesage
                        String decrypted = AESUtil.decrypt(encryptedMsg);
                        // Display the message
                        gui.appendMessage(decrypted);
                    } catch (Exception e) {
                        gui.appendMessage("[Error decrypting message]");
                    }
                }
                // If connection is lost, update indicator to any set color(red right now)
                SwingUtilities.invokeLater(() -> {
                    gui.getStatusIndicator().setBackground(Color.RED);
                });
            } catch (IOException e) {
                gui.appendMessage("Disconnected from server.");
            }
        }
    }

    // Disconnect method to close program when client leaves
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

    // Main Method
    public static void main(String[] args) {
        ChatClientGUI gui = new ChatClientGUI("User");
        try {
            // ip address goes where localhost is
            gui.connect(IP_ADDRESS, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
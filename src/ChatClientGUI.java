package src;

//this one
import java.io.IOException;

import javax.swing.*;
import java.awt.*;

public class ChatClientGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private Client client;
    private String username;
    private JButton sendButton;
    private JButton clearButton;
    private JButton logoutButton;
    private JLabel statusIndicator;
    // ChatController required for the MVC design pattern
    private ChatController controller;

    public ChatClientGUI(String username) {
        this.username = username;

        ImageIcon logo = new ImageIcon("CoolSecureChatLogo.png");
        Image img = logo.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        JLabel lblLogo = new JLabel(new ImageIcon(img));
        lblLogo.setHorizontalAlignment(JLabel.CENTER);
        add(lblLogo, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        inputField = new JTextField();

        sendButton = new JButton("Send");
        clearButton = new JButton("Clear");
        logoutButton = new JButton("Logout");

        statusIndicator = new JLabel();
        statusIndicator.setOpaque(true);
        statusIndicator.setBackground(Color.RED);
        statusIndicator.setPreferredSize(new Dimension(15, 15));
        statusIndicator.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        inputField.addActionListener(e -> {
            String msg = inputField.getText();
            // Sends the message to the ChatController class rather than directly to the
            // Client class to satisfy the MVC design pattern
            controller.sendMessage(msg);
            inputField.setText("");
        });
        sendButton.addActionListener(e -> {
            String msg = inputField.getText();
            // Sends the message to the ChatController class rather than directly to the
            // Client class to satisfy the MVC design pattern
            controller.sendMessage(msg);
            inputField.setText("");
        });
        clearButton.addActionListener(e -> {
            chatArea.setText("");
        });
        logoutButton.addActionListener(e -> {
            if (client != null) {
                // Calls the disconnect method in the ChatController class rather than directly
                // in the Client class to satisfy the MVC design pattern
                controller.disconnect();
            }
            statusIndicator.setBackground(Color.RED);
            appendMessage("You have logged out");
        });
        add(new JScrollPane(chatArea), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        JPanel statusPanel = new JPanel();
        // JPanel topPanel = new JPanel(new BorderLayout());
        buttonPanel.add(sendButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(logoutButton);
        // topPanel.add(lblLogo);
        bottomPanel.add(statusPanel, BorderLayout.WEST);
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        statusPanel.add(statusIndicator);
        add(bottomPanel, BorderLayout.SOUTH);
        setSize(400, 500);
        setVisible(true);
    }

    public void connect(String host, int port) throws IOException {
        client = new Client(host, port, this, username);
        controller = new ChatController(client);
        statusIndicator.setBackground(Color.GREEN);
    }

    public void appendMessage(String msg) {
        SwingUtilities.invokeLater(() -> chatArea.append(msg + "\n"));
    }

    public JLabel getStatusIndicator() {
        return statusIndicator;
    }

    // Class required to satisfy the MVC design pattern which separates the GUI
    // class and its responsibilities from the Client class and its responsibilities
    public class ChatController {
        private Client client;

        // Constructor method
        public ChatController(Client client) {
            this.client = client;
        }

        // Method that sends the encrypted message to the Client class to satisfy the
        // MVC design pattern
        public void sendMessage(String message) {
            client.sendMessage(message);
        }

        // Method that calls the disconnect method in the Client class to satisfy the
        // MVC design pattern
        public void disconnect() {
            client.disconnect();
        }
    }
}

package src;
/* 
Data Storage:
    Credentials are stored in credentials.txt in the format:
    user1,password1
    user2,password2
 */

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Scanner;

public class UserLoginGUI extends JFrame implements ActionListener {
    // Components for Login Forms
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;
    private JLabel lblUserStatus, lblPassStatus;

    public UserLoginGUI() {
        setTitle("Sign In to Cool Secure Chat!");
        setSize(475, 350);
        setLayout(new BorderLayout(10, 10)); // Using BorderLayout for better structure

        // Creating Panels
        JPanel mainPanel = new JPanel(new GridBagLayout()); // For Form Layout
        JPanel buttonPanel = new JPanel(new FlowLayout()); // For Buttons

        // Constraints for GridBagLayout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 20, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        // Creating, resizing, and adding logo to the top of the login GUI
        ImageIcon logo = new ImageIcon("CoolSecureChatLogo.png");
        Image img = logo.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        JLabel lblLogo = new JLabel(new ImageIcon(img));
        lblLogo.setHorizontalAlignment(JLabel.CENTER);

        add(lblLogo, BorderLayout.NORTH);

        // Username Label & TextField
        JLabel lblUser = new JLabel("Username:");
        txtUser = new JTextField(25);

        // Status Labels for Username and Password Validation
        lblUserStatus = new JLabel(" ");
        lblPassStatus = new JLabel(" ");
        Dimension statusSize = new Dimension(320, 20);
        lblUserStatus.setPreferredSize(statusSize);
        lblPassStatus.setPreferredSize(statusSize);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        mainPanel.add(lblUserStatus, gbc);
        gbc.gridy = 3;
        mainPanel.add(lblPassStatus, gbc);

        // Adding Document Listeners to validate input in real-time
        txtUser.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                validateUsername();
            }

            public void removeUpdate(DocumentEvent e) {
                validateUsername();
            }

            public void changedUpdate(DocumentEvent e) {
                validateUsername();
            }

        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(lblUser, gbc);
        gbc.gridx = 1;
        mainPanel.add(txtUser, gbc);

        // Password Label & PasswordField (Masked)
        JLabel lblPass = new JLabel("Password:");
        txtPass = new JPasswordField(25);
        txtPass.setEchoChar('*'); // Mask password input

        // Adding Document Listener to validate password in real-time
        txtPass.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                validatePassword();
            }

            public void removeUpdate(DocumentEvent e) {
                validatePassword();
            }

            public void changedUpdate(DocumentEvent e) {
                validatePassword();
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(lblPass, gbc);
        gbc.gridx = 1;
        mainPanel.add(txtPass, gbc);

        // Button for Login
        btnLogin = new JButton("Login");

        btnLogin.addActionListener(this);

        buttonPanel.add(btnLogin);

        // Adding panels to Frame
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window
        setVisible(true);
    }

    // Handling Button Clicks
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnLogin) {
            loginUser();
        }
    }

    // Methods to Validate Username and Password with Real-Time Feedback
    private void validateUsername() {
        String username = txtUser.getText();

        if (username.matches("^\\w{4,16}$")) {
            lblUserStatus.setText("Valid username");
            lblUserStatus.setForeground(Color.GREEN.darker());
        } else {
            lblUserStatus.setText("User: 4-16 characters (a-Z, 0-9, or _)");
            lblUserStatus.setForeground(Color.RED);
        }
    }

    // Password must be 8-20 characters, contain at least one uppercase letter, one
    // lowercase letter, one digit, and one special character
    private void validatePassword() {
        String password = new String(txtPass.getPassword());

        if (password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$")) {
            lblPassStatus.setText("Valid password");
            lblPassStatus.setForeground(Color.GREEN.darker());
        } else {
            lblPassStatus.setText("Pass: 8–20 characters (a-Z, 0-9, and @$!%*?&)");
            lblPassStatus.setForeground(Color.RED);
        }
    }

    // Method to Login User
    private void loginUser() {
        String username = txtUser.getText();

        String password = new String(txtPass.getPassword());

        if (!username.matches("^\\w{4,16}$")) {
            showMessage("Error", "Invalid username format.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$")) {
            showMessage("Error", "Invalid password format.", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = false;

        // Reads the credentials from credentials.txt and checks if the entered username
        // and password match any of the stored credentials
        try (Scanner sc = new Scanner(new File("credentials.txt"))) {
            while (sc.hasNextLine()) {
                String[] credentials = sc.nextLine().split(",");
                if (credentials[0].equals(username) && credentials[1].equals(password)) {
                    success = true;
                    break;
                }
            }
        } catch (Exception ex) {
            showMessage("Error", "User data file not found!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // If the credentials are valid, a successful message is shown and a new window
        // for chatting is opened
        if (success) {
            showMessage("Login Successful", "Welcome, " + username + "!", JOptionPane.INFORMATION_MESSAGE);
            try {
                ChatClientGUI chatGUI = new ChatClientGUI(username);
                chatGUI.connect("10.2.130.128", 1111);
                dispose();
            } catch (IOException e) {
                showMessage("Error", "Failed to connect to chat server.", JOptionPane.ERROR_MESSAGE);
            }
            // If the credentials are invalid, an error message is shown
        } else {
            showMessage("Login Failed", "Invalid username or password.", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to Show Message Dialog
    private void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    public static void main(String[] args) {
        new UserLoginGUI();
    }
}

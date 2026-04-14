import java.awt.*;
import java.awt.event.*;

public class ChatWindow2 extends Frame implements ActionListener {
    TextField tf1, tf2, tfResult;
    Button invite, createChat;
    Label l1, l2, l3;

    public ChatWindow2() {
        setLayout(null); // Absolute positioning

        final Color LIGHT_BLUE = new Color(51, 204,255);
        // Set background color for the frame
        setBackground(LIGHT_BLUE);

        // Create labels
        l1 = new Label("Invite Users");
        l1.setBounds(100, 90, 80, 30);
        l1.setForeground(Color.BLACK); // Set label text color

        l2 = new Label("Create Chat");
        l2.setBounds(100, 40, 80, 30);
        l2.setForeground(Color.BLACK);

        l3 = new Label("Result:");
        l3.setBounds(30, 130, 80, 30);
        l3.setForeground(Color.BLACK);

        // Create text fields
        tf1 = new TextField();
        tf1.setBounds(80, 130, 150, 30);
        tf1.setBackground(Color.WHITE); // Set background color for text fields
        tf1.setForeground(Color.BLACK); // Set text color

        // Create buttons
        invite = new Button("Invite");
        createChat = new Button("Create Chat");

        // Set button positions and sizes
        invite.setBounds(30, 180, 100, 30);
        createChat.setBounds(150, 180, 100, 30);

        // Set background colors for buttons
        invite.setBackground(Color.DARK_GRAY);
        createChat.setBackground(Color.DARK_GRAY);
        
        // Set foreground (text) color for buttons
        invite.setForeground(Color.BLACK);
        createChat.setForeground(Color.BLACK);

        // Add components to frame
        add(l1); add(tf1);
        add(l2);
        //add(l3);
        add(invite);
        add(createChat);

        // Register event listeners
        invite.addActionListener(this);
        createChat.addActionListener(this);

        setTitle("CSC+");
        setSize(300, 300); // Set window size
        setVisible(true);

        // Close window on click
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            double num1 = Double.parseDouble(tf1.getText());
            double num2 = Double.parseDouble(tf2.getText());
            double result = 0;

            if (e.getSource() == invite) {
                result = num1 + num2;
            } else if (e.getSource() == createChat) {
                result = num1 - num2;
            }
            tfResult.setText(String.valueOf(result));
        } catch (NumberFormatException ex) {
            tfResult.setText("Invalid Input");
        }
    }

    public static void main(String[] args) {
        new ChatWindow2();
    }
}
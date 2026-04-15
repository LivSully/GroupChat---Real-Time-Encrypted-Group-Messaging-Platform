package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class InviteWindowGUI extends JFrame implements ActionListener {

    private final DefaultListModel<String> master;

    // Step 1 components — name the chat
    private final JLabel l1 = new JLabel("Chat Name");
    private final JTextField nameField = new JTextField();

    // Step 2 components — invite users
    private final JLabel l2 = new JLabel("Invite Users");
    private final JTextField inviteField = new JTextField();
    private final JButton addBtn = new JButton("Add User");

    // Swing list
    private final DefaultListModel<String> invitedModel = new DefaultListModel<>();
    private final JList<String> invitedList = new JList<>(invitedModel);

    // Action buttons
    private final JButton createBtn = new JButton("Create Chat");
    private final JButton cancelBtn = new JButton("Cancel");

    // Status label
    private final JLabel statusLabel = new JLabel("");

    public InviteWindowGUI(DefaultListModel<String> master) {
        this.master = master;

        final Color LIGHT_BLUE = new Color(51, 204, 255);
        final Color DARK_GREY = Color.DARK_GRAY;
        final Color WHITE = Color.WHITE;
        final Color BLACK = Color.BLACK;

        setLayout(null);
        getContentPane().setBackground(LIGHT_BLUE);
        setTitle("CSC+ — Create Chat");

        setSize(450, 550);
        setLocationRelativeTo(null);

        // Heading
        JLabel heading = new JLabel("Create Chat");
        heading.setBounds(160, 15, 150, 25);
        heading.setForeground(BLACK);
        add(heading);

        // Chat name row
        l1.setBounds(20, 55, 80, 25);
        l1.setForeground(BLACK);
        add(l1);

        nameField.setBounds(105, 55, 250, 25);
        nameField.setBackground(WHITE);
        nameField.setForeground(BLACK);
        add(nameField);

        // Invite users row
        l2.setBounds(20, 100, 80, 25);
        l2.setForeground(BLACK);
        add(l2);

        inviteField.setBounds(105, 100, 200, 25);
        inviteField.setBackground(WHITE);
        inviteField.setForeground(BLACK);
        add(inviteField);

        addBtn.setBounds(310, 100, 100, 25);
        addBtn.setBackground(DARK_GREY);
        addBtn.setForeground(BLACK);
        add(addBtn);

        // Invited users list
        JLabel invitedLabel = new JLabel("Invited:");
        invitedLabel.setBounds(20, 140, 60, 20);
        invitedLabel.setForeground(BLACK);
        add(invitedLabel);

        JScrollPane scroll = new JScrollPane(invitedList);
        scroll.setBounds(20, 163, 390, 120);
        add(scroll);

        // Remove selected user button
        JButton removeBtn = new JButton("Remove Selected");
        removeBtn.setBounds(20, 290, 150, 25);
        removeBtn.setBackground(DARK_GREY);
        removeBtn.setForeground(BLACK);
        add(removeBtn);

        // Status label
        statusLabel.setBounds(20, 323, 390, 20);
        statusLabel.setForeground(BLACK);
        add(statusLabel);

        // Create / Cancel buttons
        createBtn.setBounds(100, 360, 120, 35);
        createBtn.setBackground(DARK_GREY);
        createBtn.setForeground(BLACK);
        add(createBtn);

        cancelBtn.setBounds(240, 360, 120, 35);
        cancelBtn.setBackground(DARK_GREY);
        cancelBtn.setForeground(BLACK);
        add(cancelBtn);

        // Listeners
        addBtn.addActionListener(this);
        removeBtn.addActionListener(this);
        createBtn.addActionListener(this);
        cancelBtn.addActionListener(this);
        inviteField.addActionListener(this);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == cancelBtn) {
            dispose();

        } else if (src == addBtn || src == inviteField) {
            String username = inviteField.getText().trim();
            if (username.isEmpty()) {
                statusLabel.setText("Enter a username first.");
                return;
            }
            if (invitedModel.contains(username)) {
                statusLabel.setText("Already added: " + username);
                return;
            }
            invitedModel.addElement(username);
            inviteField.setText("");
            statusLabel.setText("Added: " + username);

        } else if (e.getActionCommand().equals("Remove Selected")) {
            int selected = invitedList.getSelectedIndex();
            if (selected >= 0) {
                statusLabel.setText("Removed: " + invitedModel.get(selected));
                invitedModel.remove(selected);
            } else {
                statusLabel.setText("Select a user to remove.");
            }

        } else if (src == createBtn) {
            String chatName = nameField.getText().trim();
            if (chatName.isEmpty()) {
                statusLabel.setText("Please enter a chat name.");
                return;
            }

            List<String> invited = new ArrayList<>();
            for (int i = 0; i < invitedModel.size(); i++) {
                invited.add(invitedModel.get(i));
            }

            master.addElement(chatName);

            System.out.println("Group created: " + chatName + " | Invites: " + invited);

            // Confirmation dialog
            JDialog confirm = new JDialog(this, "Chat Created", true);
            confirm.setLayout(new FlowLayout());
            confirm.getContentPane().setBackground(new Color(51, 204, 255));
            confirm.setSize(300, 120);
            confirm.setLocationRelativeTo(this);

            confirm.add(new JLabel("\"" + chatName + "\" created! Invites sent to " + invited.size() + " user(s)."));

            JButton ok = new JButton("OK");
            ok.addActionListener(ev -> {
                confirm.dispose();
                dispose();
            });
            confirm.add(ok);

            confirm.setVisible(true);
        }
    }

    public static void main(String[] args) {
        new InviteWindowGUI(new DefaultListModel<>());
    }
}
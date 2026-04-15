package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class InviteWindowGUI extends JFrame implements ActionListener {

    private final Client client;
    private final String existingRoomName;

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

    public InviteWindowGUI(Client client, String existingRoomName) {
        this.client = client;
        this.existingRoomName = existingRoomName;

        final Color LIGHT_BLUE = new Color(51, 204, 255);
        final Color DARK_GREY = Color.DARK_GRAY;
        final Color WHITE = Color.WHITE;
        final Color BLACK = Color.BLACK;

        setLayout(null);
        getContentPane().setBackground(LIGHT_BLUE);
        if (existingRoomName != null) {
            setTitle("CSC+ — Add Users to " + existingRoomName);
            createBtn.setText("Add Users");
        } else {
            setTitle("CSC+ — Create Chat");
        }

        setSize(450, 550);
        setLocationRelativeTo(null);

        // Heading
        JLabel heading = new JLabel(existingRoomName != null ? "Add Users to Room" : "Create Chat");
        heading.setBounds(160, 15, 150, 25);
        heading.setForeground(BLACK);
        add(heading);

        // Only show the chat name field when creating a NEW room
        if (existingRoomName == null) {
            l1.setBounds(20, 55, 80, 25);
            l1.setForeground(BLACK);
            add(l1);

            nameField.setBounds(105, 55, 250, 25);
            nameField.setBackground(WHITE);
            nameField.setForeground(BLACK);
            add(nameField);
        } else {
            // Show the room name as a read-only label
            JLabel roomLabel = new JLabel("Room: " + existingRoomName);
            roomLabel.setBounds(20, 55, 350, 25);
            roomLabel.setForeground(BLACK);
            add(roomLabel);
        }

        // Invite users row
        l2.setBounds(20, 100, 80, 25);
        l2.setForeground(BLACK);
        add(l2);

        inviteField.setBounds(105, 100, 200, 25);
        inviteField.setBackground(WHITE);
        inviteField.setForeground(BLACK);
        add(inviteField);

        addBtn.setBounds(310, 100, 100, 25);
        addBtn.setBackground(Color.GRAY);
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
        removeBtn.setBackground(Color.GRAY);
        removeBtn.setForeground(BLACK);
        add(removeBtn);

        // Status label
        statusLabel.setBounds(20, 323, 390, 20);
        statusLabel.setForeground(BLACK);
        add(statusLabel);

        // Create / Cancel buttons
        createBtn.setBounds(100, 360, 120, 35);
        createBtn.setBackground(Color.GRAY);
        createBtn.setForeground(BLACK);
        add(createBtn);

        cancelBtn.setBounds(240, 360, 120, 35);
        cancelBtn.setBackground(Color.GRAY);
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
            // Collect invited users
            List<String> invited = new ArrayList<>();
            for (int i = 0; i < invitedModel.size(); i++) {
                invited.add(invitedModel.get(i));
            }

            if (existingRoomName != null) {
                // ── Adding users to an existing room ──
                if (invited.isEmpty()) {
                    statusLabel.setText("Add at least one user.");
                    return;
                }
                for (String user : invited) {
                    client.inviteUser(existingRoomName, user);
                }
                showConfirmation(existingRoomName, invited.size());

            } else {
                // ── Creating a brand-new room ──
                String chatName = nameField.getText().trim();
                if (chatName.isEmpty()) {
                    statusLabel.setText("Please enter a chat name.");
                    return;
                }
                // 1. Create the room on the server (client also joins automatically)
                client.createRoom(chatName);
                // 2. Invite everyone in the list
                for (String user : invited) {
                    client.inviteUser(chatName, user);
                }
                showConfirmation(chatName, invited.size());
            }
        }
    }

    private void showConfirmation(String chatName, int inviteCount) {
        JDialog confirm = new JDialog(this, "Done", true);
        confirm.setLayout(new FlowLayout());
        confirm.getContentPane().setBackground(new Color(51, 204, 255));
        confirm.setSize(300, 120);
        confirm.setLocationRelativeTo(this);
        confirm.add(new JLabel("\"" + chatName + "\" — invites sent to " + inviteCount + " user(s)."));
        JButton ok = new JButton("OK");
        ok.addActionListener(ev -> {
            confirm.dispose();
            dispose();
        });
        confirm.add(ok);
        confirm.setVisible(true);
    }
}
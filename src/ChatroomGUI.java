package src;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;

public class ChatroomGUI extends JFrame {
    private static final String APP_TITLE = "Cool Secure Chat +";
    private static final String LOGO_PATH = "CoolSecureChatLogo.png";
    private static final int LOGO_SIZE = 64;
    private static final Color SIDEBAR_BG = new Color(173, 216, 230);
    private static final Color HEADER_BG = new Color(200, 230, 245);
    private static final Color CHAT_BG = Color.WHITE;
    private static final Color MSG_SELF = new Color(220, 240, 255);
    private static final Color MSG_OTHER = new Color(240, 240, 240);
    private static final Font FONT_MAIN = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 14);
    private String username;
    private ChatController controller;
    private Client client;

    private final JPanel statusIndicator = new JPanel();
    private final JFrame frame = new JFrame(APP_TITLE);
    private final DefaultListModel<String> chatListModel = new DefaultListModel<>();
    private final JList<String> chatList = new JList<>(chatListModel);
    private final JPanel messagesPanel = new JPanel();
    private final JScrollPane messagesScroll;
    private final JTextField typingField = new JTextField();
    private String currentChat = null;

    // constructor
    public ChatroomGUI(String username) {
        this.username = username;

        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(CHAT_BG);
        messagesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        messagesScroll = new JScrollPane(messagesPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        messagesScroll.setBorder(null);
        messagesScroll.getVerticalScrollBar().setUnitIncrement(16);

        buildUI();
    }

    public void appendMessage(String text) {
        SwingUtilities.invokeLater(() -> {
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
            wrapper.setOpaque(false);
            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

            JLabel label = new JLabel(text);
            label.setFont(new Font("SansSerif", Font.PLAIN, 11));
            label.setForeground(new Color(140, 140, 140)); // small grey font
            label.setBorder(new EmptyBorder(2, 8, 2, 8));

            wrapper.add(label);
            messagesPanel.add(wrapper);
            messagesPanel.revalidate();
            scrollToBottom();
        });
    }

    // Method to set the Client instance and initialize the ChatController
    private void buildUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 660);
        frame.setMinimumSize(new Dimension(700, 500));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        frame.add(buildSidebar(), BorderLayout.WEST);
        frame.add(buildHeader(), BorderLayout.NORTH);
        frame.add(buildChatArea(), BorderLayout.CENTER);
        frame.setVisible(true);
    }

    // Sidebar with chat list
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(180, 0));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, Color.GRAY));

        JLabel chatsLabel = new JLabel("  Chats");
        chatsLabel.setFont(FONT_BOLD);
        chatsLabel.setOpaque(true);
        chatsLabel.setBackground(new Color(100, 160, 200));
        chatsLabel.setForeground(Color.WHITE);
        chatsLabel.setPreferredSize(new Dimension(180, 36));
        sidebar.add(chatsLabel, BorderLayout.NORTH);

        chatList.setFont(FONT_MAIN);
        chatList.setBackground(SIDEBAR_BG);
        chatList.setSelectionBackground(new Color(100, 160, 200));
        chatList.setSelectionForeground(Color.WHITE);
        chatList.setFixedCellHeight(52);
        chatList.setCellRenderer(new ChatCellRenderer());
        chatList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                currentChat = chatList.getSelectedValue();
                if (client != null && currentChat != null) {
                    // Tell the client which room is now open so messages route correctly
                    int idx = chatList.getSelectedIndex();
                    client.selectRoomByIndex(idx);
                    clearMessages();
                    client.openRoom(currentChat);
                }
            }
        });

        JScrollPane listScroll = new JScrollPane(chatList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listScroll.setBorder(null);
        sidebar.add(listScroll, BorderLayout.CENTER);
        return sidebar;
    }

    public void clearMessages() {
        SwingUtilities.invokeLater(() -> {
            messagesPanel.removeAll();
            messagesPanel.revalidate();
            messagesPanel.repaint();
        });
    }

    public void refreshRoomList() {
        SwingUtilities.invokeLater(() -> {
            chatListModel.clear();
            if (client != null) {
                for (String room : client.getJoinedRooms()) {
                    if (room != null) {
                        chatListModel.addElement(room);
                    }
                }
            }
        });
    }

    // Header with logo and buttons
    private JPanel buildHeader() {
        // initialization
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Color.GRAY));
        header.setPreferredSize(new Dimension(0, 100));

        // Left side: logo + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        left.setOpaque(false);
        ImageIcon logo = loadLogo(LOGO_PATH, LOGO_SIZE);
        if (logo != null)
            left.add(new JLabel(logo));
        JLabel title = new JLabel(APP_TITLE);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        left.add(title);
        header.add(left, BorderLayout.WEST);

        // Right side: buttons (Logout, Create Group)
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btns.setOpaque(false);
        // Create log out button
        JButton logoutBtn = new JButton("Log Out");
        logoutBtn.setFont(FONT_MAIN);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to log out?", "Log Out",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                frame.dispose(); // Close the current chat window
                controller.disconnect();
                new UserLoginGUI(); // Open a new login window
            }
        });
        // Create create group button
        JButton createBtn = new JButton("Create Chat");
        createBtn.setFont(FONT_MAIN);
        createBtn.setFocusPainted(false);
        createBtn.addActionListener(e -> new InviteWindowGUI(client, null).setVisible(true));
        btns.add(logoutBtn);
        btns.add(createBtn);

        JButton addUsersBtn = new JButton("Add Users");
        addUsersBtn.addActionListener(e -> {
            String currentRoom = client.getCurrentRoomName();
            if (currentRoom == null) {
                // No room selected, nothing to add to
                appendMessage("Select a room first.");
                return;
            }
            new InviteWindowGUI(client, currentRoom).setVisible(true);
        });
        btns.add(addUsersBtn);

        // Status indicator: green when connected, red on disconnect
        statusIndicator.setPreferredSize(new Dimension(12, 12));
        statusIndicator.setBackground(Color.GREEN);
        statusIndicator.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        left.add(statusIndicator);

        header.add(btns, BorderLayout.EAST);
        return header;
    }

    // Chat area with messages and input bar
    private JPanel buildChatArea() {
        JPanel chatArea = new JPanel(new BorderLayout());
        chatArea.setBackground(CHAT_BG);
        chatArea.add(messagesScroll, BorderLayout.CENTER);
        chatArea.add(buildInputBar(), BorderLayout.SOUTH);
        return chatArea;
    }

    // Input bar with text field and buttons
    private JPanel buildInputBar() {
        JPanel bar = new JPanel(new BorderLayout(4, 4));
        bar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, Color.GRAY),
                new EmptyBorder(6, 6, 6, 6)));
        bar.setBackground(new Color(245, 248, 252));

        // Left: image upload + emoji picker
        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        leftBtns.setOpaque(false);

        // creates two buttons with icons: one for image upload and one for emoji picker
        JButton imgBtn = styledIconButton("📁");
        imgBtn.addActionListener(e -> handleImageUpload());

        JButton emojiBtn = styledIconButton("☺");
        emojiBtn.addActionListener(e -> {
            EmojiPickerDialog picker = new EmojiPickerDialog(frame);
            picker.setVisible(true);
            String chosen = picker.getChosenShortcode(); // shows the shortcode of the chosen emoji
            if (chosen != null)
                insertAtCaret(chosen + " ");
        });

        leftBtns.add(imgBtn);
        leftBtns.add(emojiBtn);
        bar.add(leftBtns, BorderLayout.WEST);

        // Centre: typing field
        typingField.setFont(FONT_MAIN);
        typingField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        typingField.addKeyListener(new KeyAdapter() {
            @Override
            // When Enter is pressed, send the message
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    sendMessage();
            }
        });
        bar.add(typingField, BorderLayout.CENTER);

        // Right: clear + send
        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        rightBtns.setOpaque(false);

        JButton clearBtn = styledIconButton("C");
        clearBtn.addActionListener(e -> typingField.setText(""));

        JButton sendBtn = styledIconButton("⬆");
        sendBtn.addActionListener(e -> sendMessage());

        rightBtns.add(clearBtn);
        rightBtns.add(sendBtn);
        bar.add(rightBtns, BorderLayout.EAST);
        return bar;
    }

    public void connect(String host, int port) throws IOException {
        client = new Client(host, port, this, username);
        controller = new ChatController(client);
    }

    // message sending and receiving methods

    // called when the user clicks send or presses Enter, to encrypt and send the
    // message
    private void sendMessage() {
        String raw = typingField.getText().trim();
        if (raw.isEmpty())
            return;

        // Encode any raw Unicode emoji → :shortcode: before encrypting
        String encoded = EmojiRegistry.encode(raw);

        // Display immediately on our side (render shortcodes back to emoji for display)
        typingField.setText("");
        scrollToBottom();

        // Hand off to client → AESUtil.encrypt → server
        if (controller != null) {
            controller.sendMessage(encoded);
        } else {
            appendMessage("Not connected — message not sent.");
        }
    }

    // call this with decrypted text received from the server to display it in the
    // chat area
    public void receiveMessage(String encodedMessage) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("[RECV encoded]  " + encodedMessage);
            addMessageBubble(EmojiRegistry.render(encodedMessage), false);
            scrollToBottom();
        });
    }

    public void receiveImage(String room, String sender, String fileName, byte[] imageBytes) {
        SwingUtilities.invokeLater(() -> {
            // Scale to max 300 px wide for display
            ImageIcon icon = new ImageIcon(imageBytes);
            Image scaled = icon.getImage().getScaledInstance(300, -1, Image.SCALE_SMOOTH);

            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            wrapper.setOpaque(false);
            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

            JPanel bubble = new JPanel(new BorderLayout(0, 4));
            bubble.setBackground(MSG_OTHER);
            bubble.setBorder(new CompoundBorder(
                    new LineBorder(new Color(200, 200, 200), 1, true),
                    new EmptyBorder(6, 10, 6, 10)));

            // Sender + filename caption above image
            JLabel caption = new JLabel(sender + ": " + fileName);
            caption.setFont(new Font("SansSerif", Font.ITALIC, 11));
            caption.setForeground(new Color(100, 100, 100));
            bubble.add(caption, BorderLayout.NORTH);

            JLabel imgLabel = new JLabel(new ImageIcon(scaled));
            bubble.add(imgLabel, BorderLayout.CENTER);

            wrapper.add(bubble);
            messagesPanel.add(wrapper);
            messagesPanel.revalidate();
            scrollToBottom();
        });
    }

    public JPanel getStatusIndicator() {
        return statusIndicator;
    }

    // Append a rendered bubble to the chat panel
    public void addMessageBubble(String renderedText, boolean isSelf) {
        SwingUtilities.invokeLater(() -> {
            JPanel wrapper = new JPanel(
                    new FlowLayout(isSelf ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 4));
            wrapper.setOpaque(false);
            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

            JTextArea bubble = new JTextArea(renderedText);
            bubble.setFont(FONT_MAIN);
            bubble.setLineWrap(true);
            bubble.setWrapStyleWord(true);
            bubble.setEditable(false);
            bubble.setFocusable(false);
            bubble.setBackground(isSelf ? MSG_SELF : MSG_OTHER);
            bubble.setBorder(new CompoundBorder(
                    new LineBorder(isSelf ? new Color(160, 200, 230)
                            : new Color(200, 200, 200), 1, true),
                    new EmptyBorder(6, 10, 6, 10)));

            bubble.setSize(new Dimension(500, Short.MAX_VALUE));
            Dimension pref = bubble.getPreferredSize();
            bubble.setPreferredSize(new Dimension(Math.min(pref.width + 20, 500), pref.height));

            wrapper.add(bubble);
            messagesPanel.add(wrapper);
            messagesPanel.revalidate();
        });
    }

    // Image upload handler
    private void handleImageUpload() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select an image to send");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files", "png", "jpg", "jpeg", "gif", "bmp", "webp"));

        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
            return;

        File file = chooser.getSelectedFile();
        try {
            // Preview locally
            Image scaled = new ImageIcon(file.getAbsolutePath())
                    .getImage().getScaledInstance(300, -1, Image.SCALE_SMOOTH);

            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
            wrapper.setOpaque(false);
            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

            JPanel bubble = new JPanel(new BorderLayout(0, 4));
            bubble.setBackground(MSG_SELF);
            bubble.setBorder(new CompoundBorder(
                    new LineBorder(new Color(160, 200, 230), 1, true),
                    new EmptyBorder(6, 10, 6, 10)));

            JLabel caption = new JLabel(username + ": " + file.getName());
            caption.setFont(new Font("SansSerif", Font.ITALIC, 11));
            caption.setForeground(new Color(100, 100, 100));
            bubble.add(caption, BorderLayout.NORTH);
            bubble.add(new JLabel(new ImageIcon(scaled)), BorderLayout.CENTER);

            wrapper.add(bubble);
            messagesPanel.add(wrapper);
            messagesPanel.revalidate();
            scrollToBottom();

            // Encrypt and transmit via client
            if (controller != null) {
                controller.sendImage(file);
            } else {
                appendMessage("Not connected — image not sent.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    "Could not load image: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Scroll the chat to the bottom to show the latest message
    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar sb = messagesScroll.getVerticalScrollBar();
            sb.setValue(sb.getMaximum());
        });
    }

    // Insert text at the current caret position in the typing field
    private void insertAtCaret(String text) {
        int pos = typingField.getCaretPosition();
        String cur = typingField.getText();
        typingField.setText(cur.substring(0, pos) + text + cur.substring(pos));
        typingField.setCaretPosition(pos + text.length());
        typingField.requestFocus();
    }

    // loads our logo image and scales it to the desired size, returning an
    // ImageIcon
    private static ImageIcon loadLogo(String path, int size) {
        File f = new File(path);
        if (!f.exists()) {
            System.err.println("Logo not found: " + f.getAbsolutePath());
            return null;
        }
        Image img = new ImageIcon(path).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    // Helper to create buttons with icons
    private static JButton styledIconButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(FONT_BOLD);
        btn.setPreferredSize(new Dimension(40, 36));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Cell renderer for the chat list to add padding and custom background
    private static class ChatCellRenderer extends DefaultListCellRenderer {
        private static final Font F = new Font("SansSerif", Font.PLAIN, 14);
        private static final Color BG = new Color(173, 216, 230);

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int idx, boolean sel, boolean focus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, idx, sel, focus);
            lbl.setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 0, new Color(150, 190, 210)),
                    new EmptyBorder(8, 12, 8, 8)));
            lbl.setFont(F);
            if (!sel)
                lbl.setBackground(BG);
            return lbl;
        }
    }

    // Controller class to handle interactions between the GUI and the Client (MVC
    // pattern)
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

        // Method that sends the image file to the Client class to satisfy the
        // MVC design pattern
        public void sendImage(File image) {
            client.sendImage(image);
        }

        // Method that calls the disconnect method in the Client class to satisfy the
        // MVC design pattern
        public void disconnect() {
            client.disconnect();
        }
    }
}

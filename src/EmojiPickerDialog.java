package src;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// model showing all emojis and their shortcodes, with a search box to filter by name
public class EmojiPickerDialog extends JDialog {

    private static final int COLS = 8;
    private static final int BTN_SIZE = 44;
    private static final Font EMOJI_FONT = new Font("Segoe UI Emoji", Font.PLAIN, 22);
    private static final Font SEARCH_FONT = new Font("SansSerif", Font.PLAIN, 13);

    private String chosenShortcode = null;

    // list of all entries for filtering
    private final List<Map.Entry<String, String>> allEntries;

    // The grid panel that gets rebuilt on each filter
    private final JPanel gridPanel = new JPanel();
    private final JTextField searchField = new JTextField();
    private final JLabel hintLabel = new JLabel(" ");

    // Constructor: initializes the dialog, builds the UI, and populates the grid
    // with all emojis
    public EmojiPickerDialog(Frame parent) {
        super(parent, "Insert Emoji", true);

        allEntries = new ArrayList<>(EmojiRegistry.getMap().entrySet());

        buildUI();
        populateGrid(""); // show all on open

        setSize(420, 460);
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    // building the UI
    private void buildUI() {
        setLayout(new BorderLayout(6, 6));
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        // search bar at the top
        JPanel topPanel = new JPanel(new BorderLayout(6, 0));
        topPanel.setOpaque(false);

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(EMOJI_FONT);
        topPanel.add(searchIcon, BorderLayout.WEST);

        searchField.setFont(SEARCH_FONT);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        });
        topPanel.add(searchField, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Emoji grid (scrollable)
        gridPanel.setLayout(new GridLayout(0, COLS, 2, 2));
        gridPanel.setBackground(Color.WHITE);

        // Wrap grid in scroll pane to allow scrolling when there are many emojis
        JScrollPane scroll = new JScrollPane(gridPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // Bottom: shortcode hint + cancel
        JPanel bottomPanel = new JPanel(new BorderLayout(6, 0));
        bottomPanel.setOpaque(false);

        hintLabel.setFont(new Font("Monospaced", Font.ITALIC, 12));
        hintLabel.setForeground(new Color(90, 90, 90));
        bottomPanel.add(hintLabel, BorderLayout.CENTER);

        // Cancel button on the right
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        bottomPanel.add(cancelBtn, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // filtering logic: rebuild the grid based on the search query
    private void filter() {
        populateGrid(searchField.getText().trim().toLowerCase());
    }

    // populates the grid with emojis whose shortcodes contain the query string (or
    // all if query is empty)
    private void populateGrid(String query) {
        gridPanel.removeAll();

        for (Map.Entry<String, String> entry : allEntries) {
            String code = entry.getKey(); // e.g. "smile"
            String emoji = entry.getValue(); // e.g. "😊"

            if (!query.isEmpty() && !code.contains(query))
                continue;

            JButton btn = new JButton(emoji);
            btn.setFont(EMOJI_FONT);
            btn.setToolTipText(":" + code + ":");
            btn.setPreferredSize(new Dimension(BTN_SIZE, BTN_SIZE));
            btn.setMargin(new Insets(0, 0, 0, 0));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            btn.setBackground(Color.WHITE);
            btn.setOpaque(true);

            // Hover: show shortcode hint
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hintLabel.setText(":" + code + ":");
                    btn.setBackground(new Color(220, 235, 255));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hintLabel.setText(" ");
                    btn.setBackground(Color.WHITE);
                }
            });

            // Click: choose this emoji
            btn.addActionListener(e -> {
                chosenShortcode = ":" + code + ":";
                dispose();
            });

            gridPanel.add(btn);
        }

        // Pad last row so GridLayout doesn't stretch final button
        int count = gridPanel.getComponentCount();
        int remainder = count % COLS;
        if (remainder != 0) {
            for (int i = 0; i < COLS - remainder; i++) {
                gridPanel.add(new JPanel() {
                    {
                        setOpaque(false);
                    }
                });
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    // After the dialog is closed, this method can be called to get the chosen
    // shortcode (or null if cancelled)
    public String getChosenShortcode() {
        return chosenShortcode;
    }
}

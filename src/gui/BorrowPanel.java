package gui;

import controller.BorrowController;
import controller.LibraryManager;
import model.LibraryItem;
import model.UserAccount;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.util.List;

/**
 * "Borrow/Return" tab. Layout: BorderLayout with a GridBagLayout form up top.
 * Demonstrates event-driven programming (button clicks, combo selections)
 * and a javax.swing.Timer that periodically checks for overdue items and
 * updates a reminder label (advanced GUI technique: timers).
 */
public class BorrowPanel extends JPanel {

    private final MainWindow mainWindow;
    private final LibraryManager manager;
    private final BorrowController borrowController;

    private final JComboBox<String> userCombo = new JComboBox<>();
    private final JComboBox<String> itemCombo = new JComboBox<>();
    private final JLabel reminderLabel = new JLabel(" ");
    private final ItemTableModel borrowedTableModel = new ItemTableModel();
    private final JTable borrowedTable = new JTable(borrowedTableModel);

    public BorrowPanel(MainWindow mainWindow, LibraryManager manager, BorrowController borrowController) {
        this.mainWindow = mainWindow;
        this.manager = manager;
        this.borrowController = borrowController;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildFormPanel(), BorderLayout.NORTH);

        borrowedTable.setRowHeight(22);
        add(new JScrollPane(borrowedTable), BorderLayout.CENTER);

        reminderLabel.setForeground(new Color(178, 34, 34));
        add(reminderLabel, BorderLayout.SOUTH);

        startOverdueTimer();
        refresh();
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Borrow / Return"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("User:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(userCombo, gbc);

        JButton newUserBtn = new JButton("New User...");
        newUserBtn.setToolTipText("Register a new library patron");
        newUserBtn.addActionListener(e -> registerNewUser());
        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(newUserBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(new JLabel("Item:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(itemCombo, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton borrowBtn = new JButton("Borrow");
        borrowBtn.setMnemonic('B');
        borrowBtn.setToolTipText("Check out the selected item to the selected user");
        borrowBtn.addActionListener(e -> doBorrow());

        JButton returnBtn = new JButton("Return");
        returnBtn.setMnemonic('R');
        returnBtn.setToolTipText("Return the selected item");
        returnBtn.addActionListener(e -> doReturn());

        buttons.add(borrowBtn);
        buttons.add(returnBtn);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        panel.add(buttons, gbc);

        return panel;
    }

    private void registerNewUser() {
        String name = JOptionPane.showInputDialog(this, "New user's full name:", "Register User",
                JOptionPane.PLAIN_MESSAGE);
        if (name == null) return; // cancelled
        if (name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Invalid input", JOptionPane.ERROR_MESSAGE);
            return;
        }
        UserAccount user = manager.registerUser(name.trim());
        mainWindow.setStatus("Registered new user " + user.getUserId() + " - " + user.getName());
        refresh();
    }

    private void doBorrow() {
        String itemId = extractId((String) itemCombo.getSelectedItem());
        String userId = extractId((String) userCombo.getSelectedItem());
        if (itemId == null || userId == null) {
            JOptionPane.showMessageDialog(this, "Please select both a user and an item.",
                    "Missing selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String result = borrowController.borrow(itemId, userId);
        mainWindow.setStatus(result);
        mainWindow.refreshAll();
    }

    private void doReturn() {
        String itemId = extractId((String) itemCombo.getSelectedItem());
        if (itemId == null) {
            JOptionPane.showMessageDialog(this, "Please select an item to return.",
                    "Missing selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String result = borrowController.returnItem(itemId);
        mainWindow.setStatus(result);
        mainWindow.refreshAll();
    }

    private String extractId(String comboText) {
        if (comboText == null) return null;
        int dash = comboText.indexOf(" - ");
        return dash < 0 ? comboText : comboText.substring(0, dash);
    }

    private void startOverdueTimer() {
        Timer timer = new Timer(15000, e -> checkOverdue());
        timer.setInitialDelay(2000);
        timer.start();
    }

    private void checkOverdue() {
        List<UserAccount> overdueUsers = manager.overdueUsersReport();
        if (overdueUsers.isEmpty()) {
            reminderLabel.setText(" ");
        } else {
            StringBuilder sb = new StringBuilder(" Overdue reminder: ");
            for (UserAccount u : overdueUsers) {
                sb.append(u.getName()).append(" (").append(u.getOverdueItemIds().size()).append(" item(s)) ");
            }
            reminderLabel.setText(sb.toString());
        }
    }

    public void refresh() {
        userCombo.removeAllItems();
        for (UserAccount u : manager.getDatabase().getUsers().values()) {
            userCombo.addItem(u.getUserId() + " - " + u.getName());
        }
        itemCombo.removeAllItems();
        for (LibraryItem item : manager.getDatabase().getItems()) {
            String status = item.isBorrowed() ? "borrowed" : "available";
            itemCombo.addItem(item.getId() + " - " + item.getTitle() + " (" + status + ")");
        }

        java.util.List<LibraryItem> borrowed = new java.util.ArrayList<>();
        for (LibraryItem item : manager.getDatabase().getItems()) {
            if (item.isBorrowed()) borrowed.add(item);
        }
        borrowedTableModel.setItems(borrowed);

        checkOverdue();
    }
}

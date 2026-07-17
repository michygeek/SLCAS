package gui;

import controller.LibraryManager;
import model.Book;
import model.Journal;
import model.LibraryItem;
import model.Magazine;
import model.UserAccount;
import utils.FileHandler;
import utils.IDGenerator;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;

/**
 * "Admin" tab. Uses CardLayout to switch between three screens (Add Item,
 * Delete Item, Reports) driven by a row of toolbar buttons - this is the
 * CardLayout requirement from the spec. Also demonstrates:
 *  - dynamic components (the extra-field area changes with item type)
 *  - a JFileChooser for import/export
 *  - input validation via dialog popups
 *  - the undo Stack
 */
public class AdminPanel extends JPanel {

    private final MainWindow mainWindow;
    private final LibraryManager manager;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel();

    // --- Add Item card widgets ---
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Book", "Magazine", "Journal"});
    private final JTextField titleField = new JTextField(18);
    private final JTextField authorField = new JTextField(18);
    private final JTextField yearField = new JTextField(6);
    private final JTextField categoryField = new JTextField(12);
    private final CardLayout extraCardLayout = new CardLayout();
    private final JPanel extraFieldPanel = new JPanel(extraCardLayout);
    private final JTextField isbnField = new JTextField(12);
    private final JTextField issueField = new JTextField(12);
    private final JTextField volumeField = new JTextField(6);
    private final JTextField journalIssueField = new JTextField(6);

    // --- Delete Item card widgets ---
    private final ItemTableModel deleteTableModel = new ItemTableModel();
    private final JTable deleteTable = new JTable(deleteTableModel);

    // --- Reports card widgets ---
    private final JTextArea reportArea = new JTextArea();

    public AdminPanel(MainWindow mainWindow, LibraryManager manager) {
        this.mainWindow = mainWindow;
        this.manager = manager;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildToolbar(), BorderLayout.NORTH);

        cards.setLayout(cardLayout);
        cards.add(buildAddItemCard(), "ADD");
        cards.add(buildDeleteItemCard(), "DELETE");
        cards.add(buildReportsCard(), "REPORTS");
        add(cards, BorderLayout.CENTER);

        cardLayout.show(cards, "ADD");
        refresh();
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addScreenBtn = new JButton("Add Item");
        addScreenBtn.setMnemonic('A');
        addScreenBtn.addActionListener(e -> cardLayout.show(cards, "ADD"));

        JButton deleteScreenBtn = new JButton("Delete Item");
        deleteScreenBtn.setMnemonic('D');
        deleteScreenBtn.addActionListener(e -> cardLayout.show(cards, "DELETE"));

        JButton reportsScreenBtn = new JButton("Reports");
        reportsScreenBtn.setMnemonic('R');
        reportsScreenBtn.addActionListener(e -> { cardLayout.show(cards, "REPORTS"); refreshReports(); });

        JButton undoBtn = new JButton("Undo Last Action");
        undoBtn.setToolTipText("Reverses the most recent add/delete (Ctrl+Z)");
        undoBtn.addActionListener(e -> {
            String result = manager.undo();
            mainWindow.setStatus(result);
            mainWindow.refreshAll();
        });

        JButton importBtn = new JButton("Import...");
        importBtn.setToolTipText("Load catalogue data from a JSON file");
        importBtn.addActionListener(e -> doImport());

        JButton exportBtn = new JButton("Export...");
        exportBtn.setToolTipText("Save catalogue data to a JSON file");
        exportBtn.addActionListener(e -> doExport());

        toolbar.add(addScreenBtn);
        toolbar.add(deleteScreenBtn);
        toolbar.add(reportsScreenBtn);
        toolbar.add(undoBtn);
        toolbar.add(importBtn);
        toolbar.add(exportBtn);
        return toolbar;
    }

    // ---------------- Add Item card ----------------

    private JPanel buildAddItemCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; panel.add(typeCombo, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; panel.add(titleField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; panel.add(authorField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; panel.add(yearField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; panel.add(categoryField, gbc);
        row++;

        // Dynamic component area: the fields shown here change depending on
        // the chosen type, added/removed at runtime via CardLayout.
        JPanel isbnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        isbnPanel.add(new JLabel("ISBN: "));
        isbnPanel.add(isbnField);

        JPanel issuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        issuePanel.add(new JLabel("Issue #: "));
        issuePanel.add(issueField);

        JPanel journalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        journalPanel.add(new JLabel("Volume: "));
        journalPanel.add(volumeField);
        journalPanel.add(new JLabel("  Issue: "));
        journalPanel.add(journalIssueField);

        extraFieldPanel.add(isbnPanel, "Book");
        extraFieldPanel.add(issuePanel, "Magazine");
        extraFieldPanel.add(journalPanel, "Journal");

        typeCombo.addActionListener(e ->
                extraCardLayout.show(extraFieldPanel, (String) typeCombo.getSelectedItem()));

        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Details:"), gbc);
        gbc.gridx = 1; gbc.gridy = row; panel.add(extraFieldPanel, gbc);
        row++;

        JButton addBtn = new JButton("Add Item");
        addBtn.setToolTipText("Validates the fields and adds a new catalogue item");
        addBtn.addActionListener(e -> doAddItem());
        gbc.gridx = 1; gbc.gridy = row; panel.add(addBtn, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(panel, BorderLayout.NORTH);
        return wrapper;
    }

    private void doAddItem() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String yearText = yearField.getText().trim();
        String category = categoryField.getText().trim();
        String type = (String) typeCombo.getSelectedItem();

        // Input validation with dialog popups (advanced GUI technique).
        if (title.isEmpty() || author.isEmpty() || category.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title, author and category are all required.",
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int year;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Year must be a whole number, e.g. 2023.",
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String id = IDGenerator.nextItemId();
        LibraryItem item;
        switch (type) {
            case "Magazine":
                item = new Magazine(id, title, author, year, category, issueField.getText().trim());
                break;
            case "Journal":
                item = new Journal(id, title, author, year, category,
                        volumeField.getText().trim(), journalIssueField.getText().trim());
                break;
            case "Book":
            default:
                item = new Book(id, title, author, year, category, isbnField.getText().trim());
                break;
        }

        manager.addItem(item);
        mainWindow.setStatus("Added " + type + " '" + title + "' (" + id + ")");
        clearAddForm();
        mainWindow.refreshAll();
    }

    private void clearAddForm() {
        titleField.setText("");
        authorField.setText("");
        yearField.setText("");
        categoryField.setText("");
        isbnField.setText("");
        issueField.setText("");
        volumeField.setText("");
        journalIssueField.setText("");
    }

    // ---------------- Delete Item card ----------------

    private JPanel buildDeleteItemCard() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        deleteTable.setRowHeight(22);
        panel.add(new JScrollPane(deleteTable), BorderLayout.CENTER);

        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.setToolTipText("Removes the selected item (can be undone)");
        deleteBtn.addActionListener(e -> doDelete());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(deleteBtn);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void doDelete() {
        int row = deleteTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an item to delete.",
                    "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LibraryItem item = deleteTableModel.getItemAt(deleteTable.convertRowIndexToModel(row));
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete '" + item.getTitle() + "'? This can be undone from the toolbar.",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String result = manager.deleteItem(item.getId());
        mainWindow.setStatus(result);
        mainWindow.refreshAll();
    }

    // ---------------- Reports card ----------------

    private JPanel buildReportsCard() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);

        JButton generateBtn = new JButton("Generate Reports");
        generateBtn.addActionListener(e -> refreshReports());
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(generateBtn);
        panel.add(north, BorderLayout.NORTH);
        return panel;
    }

    private void refreshReports() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Most Borrowed Items ===\n");
        for (LibraryItem item : manager.mostBorrowedItemsReport()) {
            sb.append(String.format("  %-30s accessed %d time(s)%n", item.getTitle(), item.getAccessCount()));
        }
        if (manager.mostBorrowedItemsReport().isEmpty()) sb.append("  (no borrowing activity yet)\n");

        sb.append("\n=== Users With Overdue Items ===\n");
        for (UserAccount u : manager.overdueUsersReport()) {
            sb.append(String.format("  %s - %d overdue item(s)%n", u.getName(), u.getOverdueItemIds().size()));
        }
        if (manager.overdueUsersReport().isEmpty()) sb.append("  (no overdue items)\n");

        sb.append("\n=== Category Distribution ===\n");
        for (Map.Entry<String, Integer> e : manager.categoryDistributionReport().entrySet()) {
            sb.append(String.format("  %-20s %d item(s)%n", e.getKey(), e.getValue()));
        }

        reportArea.setText(sb.toString());
    }

    // ---------------- Import / Export ----------------

    private void doImport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import Library Data (JSON)");
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;
        try {
            FileHandler.loadDatabase(manager.getDatabase(), chooser.getSelectedFile().getAbsolutePath());
            mainWindow.setStatus("Imported data from " + chooser.getSelectedFile().getName());
            mainWindow.refreshAll();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage(),
                    "Import Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doExport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Library Data (JSON)");
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;
        try {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith(".json")) path += ".json";
            FileHandler.saveDatabase(manager.getDatabase(), path);
            mainWindow.setStatus("Exported data to " + path);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refresh() {
        deleteTableModel.setItems(manager.getDatabase().getItems());
    }
}

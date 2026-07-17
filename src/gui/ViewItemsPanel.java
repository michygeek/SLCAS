package gui;

import controller.LibraryManager;
import model.LibraryItem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * "View Items" tab: shows every catalogue item in a JTable, with a custom
 * cell renderer that colours rows green (available) or salmon (borrowed)
 * for at-a-glance status - one of the "advanced GUI techniques".
 * Layout: BorderLayout (table in center, toolbar in north).
 */
public class ViewItemsPanel extends JPanel {

    private final MainWindow mainWindow;
    private final LibraryManager manager;
    private final ItemTableModel tableModel = new ItemTableModel();
    private final JTable table = new JTable(tableModel);

    public ViewItemsPanel(MainWindow mainWindow, LibraryManager manager) {
        this.mainWindow = mainWindow;
        this.manager = manager;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        table.setRowHeight(24);
        table.setDefaultRenderer(Object.class, new StatusRowRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setToolTipText("Reload the table from the current catalogue state");
        refreshBtn.addActionListener(e -> refresh());
        JButton detailsBtn = new JButton("View Details");
        detailsBtn.setToolTipText("Show full information for the selected item");
        detailsBtn.addActionListener(e -> showDetails());
        toolbar.add(refreshBtn);
        toolbar.add(detailsBtn);
        add(toolbar, BorderLayout.NORTH);

        refresh();
    }

    private void showDetails() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an item first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LibraryItem item = tableModel.getItemAt(table.convertRowIndexToModel(row));
        // Polymorphism in action: displayInfo() resolves to the correct
        // subclass override regardless of the declared LibraryItem type.
        JOptionPane.showMessageDialog(this, LibraryManager.describeAny(item), "Item Details",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void refresh() {
        tableModel.setItems(manager.getDatabase().getItems());
    }

    /** Custom renderer: colours a row based on borrowed/overdue/available state. */
    private class StatusRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                LibraryItem item = tableModel.getItemAt(table.convertRowIndexToModel(row));
                if (!item.isBorrowed()) {
                    c.setBackground(new Color(224, 245, 228)); // light green
                } else if (item.getDueDate() != null && item.getDueDate().isBefore(java.time.LocalDate.now())) {
                    c.setBackground(new Color(250, 214, 214)); // overdue red
                } else {
                    c.setBackground(new Color(255, 244, 214)); // borrowed amber
                }
            } else {
                c.setBackground(table.getSelectionBackground());
            }
            return c;
        }
    }
}

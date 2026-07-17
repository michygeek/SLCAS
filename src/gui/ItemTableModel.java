package gui;

import model.LibraryItem;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model that adapts a List<LibraryItem> (any mix of Book / Magazine /
 * Journal, thanks to polymorphism) into rows for a JTable.
 */
public class ItemTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
            "ID", "Type", "Title", "Author", "Year", "Category", "Status"
    };

    private List<LibraryItem> items = new ArrayList<>();

    public void setItems(List<LibraryItem> items) {
        this.items = items;
        fireTableDataChanged();
    }

    public LibraryItem getItemAt(int row) { return items.get(row); }

    @Override
    public int getRowCount() { return items.size(); }

    @Override
    public int getColumnCount() { return COLUMNS.length; }

    @Override
    public String getColumnName(int column) { return COLUMNS[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LibraryItem item = items.get(rowIndex);
        switch (columnIndex) {
            case 0: return item.getId();
            case 1: return item.getType();
            case 2: return item.getTitle();
            case 3: return item.getAuthor();
            case 4: return item.getYear();
            case 5: return item.getCategory();
            case 6:
                if (!item.isBorrowed()) return "Available";
                return "Borrowed by " + item.getBorrowedBy() + " (due " + item.getDueDate() + ")";
            default: return "";
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) { return false; }
}

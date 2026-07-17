package gui;

import controller.LibraryManager;
import controller.SearchEngine;
import controller.SortEngine;
import model.LibraryItem;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * "Search & Sort" tab. Layout: GridBagLayout for the control form (as
 * encouraged by the spec), with a results table below. Lets the user pick,
 * via combo boxes, which student-implemented search algorithm and which
 * student-implemented sort algorithm to run.
 */
public class SearchSortPanel extends JPanel {

    private final MainWindow mainWindow;
    private final LibraryManager manager;

    private final JTextField queryField = new JTextField(16);
    private final JComboBox<SearchEngine.Field> searchFieldCombo = new JComboBox<>(SearchEngine.Field.values());
    private final JComboBox<String> searchAlgoCombo = new JComboBox<>(new String[]{"Linear", "Binary", "Recursive"});

    private final JComboBox<SortEngine.Field> sortFieldCombo = new JComboBox<>(SortEngine.Field.values());
    private final JComboBox<SortEngine.Algorithm> sortAlgoCombo = new JComboBox<>(SortEngine.Algorithm.values());

    private final ItemTableModel resultsModel = new ItemTableModel();
    private final JTable resultsTable = new JTable(resultsModel);

    public SearchSortPanel(MainWindow mainWindow, LibraryManager manager) {
        this.mainWindow = mainWindow;
        this.manager = manager;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildControlPanel(), BorderLayout.NORTH);
        resultsTable.setRowHeight(22);
        add(new JScrollPane(resultsTable), BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Search & Sort"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // --- Search row ---
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Search for:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(queryField, gbc);
        gbc.gridx = 2; gbc.gridy = 0; panel.add(new JLabel("in field:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; panel.add(searchFieldCombo, gbc);
        gbc.gridx = 4; gbc.gridy = 0; panel.add(new JLabel("using:"), gbc);
        gbc.gridx = 5; gbc.gridy = 0; panel.add(searchAlgoCombo, gbc);

        JButton searchBtn = new JButton("Search");
        searchBtn.setToolTipText("Binary search requires the list to be sorted first by the same field - it will sort a copy automatically.");
        searchBtn.addActionListener(e -> doSearch());
        gbc.gridx = 6; gbc.gridy = 0; panel.add(searchBtn, gbc);

        // --- Sort row ---
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Sort by:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(sortFieldCombo, gbc);
        gbc.gridx = 2; gbc.gridy = 1; panel.add(new JLabel("using:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; panel.add(sortAlgoCombo, gbc);

        JButton sortBtn = new JButton("Sort");
        sortBtn.addActionListener(e -> doSort());
        gbc.gridx = 4; gbc.gridy = 1; panel.add(sortBtn, gbc);

        JButton resetBtn = new JButton("Show All");
        resetBtn.addActionListener(e -> refresh());
        gbc.gridx = 5; gbc.gridy = 1; panel.add(resetBtn, gbc);

        return panel;
    }

    private void doSearch() {
        String query = queryField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Type something to search for first.",
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SearchEngine.Field field = (SearchEngine.Field) searchFieldCombo.getSelectedItem();
        String algo = (String) searchAlgoCombo.getSelectedItem();
        List<LibraryItem> all = manager.getDatabase().getItems();

        List<LibraryItem> results = new ArrayList<>();
        switch (algo) {
            case "Linear":
                results = SearchEngine.linearSearch(all, query, field);
                break;
            case "Recursive":
                results = SearchEngine.recursiveSearch(all, query, field);
                break;
            case "Binary": {
                // Binary search needs an ordering, so sort a copy by the
                // matching field first, then search for an exact match.
                SortEngine.Field sortField = mapToSortField(field);
                if (sortField == null) {
                    JOptionPane.showMessageDialog(this,
                            "Binary search needs a sortable field (Title or Author), not Type.",
                            "Unsupported field", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                List<LibraryItem> sorted = SortEngine.sort(all, sortField, SortEngine.Algorithm.QUICK);
                LibraryItem found = SearchEngine.binarySearch(sorted, query, field);
                if (found != null) results.add(found);
                break;
            }
        }

        resultsModel.setItems(results);
        mainWindow.setStatus(results.size() + " result(s) found for \"" + query + "\" using " + algo + " search.");
    }

    private SortEngine.Field mapToSortField(SearchEngine.Field field) {
        switch (field) {
            case TITLE: return SortEngine.Field.TITLE;
            case AUTHOR: return SortEngine.Field.AUTHOR;
            default: return null; // "type" has no matching sortable field
        }
    }

    private void doSort() {
        SortEngine.Field field = (SortEngine.Field) sortFieldCombo.getSelectedItem();
        SortEngine.Algorithm algo = (SortEngine.Algorithm) sortAlgoCombo.getSelectedItem();
        List<LibraryItem> sorted = SortEngine.sort(manager.getDatabase().getItems(), field, algo);
        resultsModel.setItems(sorted);
        mainWindow.setStatus("Sorted " + sorted.size() + " item(s) by " + field + " using " + algo + " sort.");
    }

    public void refresh() {
        resultsModel.setItems(manager.getDatabase().getItems());
    }
}

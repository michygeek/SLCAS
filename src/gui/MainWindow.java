package gui;

import controller.BorrowController;
import controller.LibraryManager;
import model.LibraryDatabase;
import utils.FileHandler;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Top level application window.
 * Layout: BorderLayout with a JTabbedPane in the center and a status bar
 * along the south edge. Demonstrates a menu bar with mnemonics and
 * keyboard shortcuts (advanced GUI technique).
 */
public class MainWindow extends JFrame {

    public static final String DEFAULT_DATA_FILE = "data/library_data.json";

    private final LibraryDatabase db = new LibraryDatabase();
    private final LibraryManager manager = new LibraryManager(db);
    private final BorrowController borrowController = new BorrowController(db);

    private final JLabel statusBar = new JLabel(" Ready");

    private ViewItemsPanel viewItemsPanel;
    private BorrowPanel borrowPanel;
    private AdminPanel adminPanel;
    private SearchSortPanel searchSortPanel;

    public MainWindow() {
        super("Smart Library Circulation & Automation System (SLCAS)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        loadInitialData();

        setJMenuBar(buildMenuBar());

        JTabbedPane tabs = new JTabbedPane();
        viewItemsPanel = new ViewItemsPanel(this, manager);
        borrowPanel = new BorrowPanel(this, manager, borrowController);
        adminPanel = new AdminPanel(this, manager);
        searchSortPanel = new SearchSortPanel(this, manager);

        tabs.addTab("View Items", viewItemsPanel);
        tabs.addTab("Borrow/Return", borrowPanel);
        tabs.addTab("Admin", adminPanel);
        tabs.addTab("Search & Sort", searchSortPanel);
        tabs.setMnemonicAt(0, java.awt.event.KeyEvent.VK_V);
        tabs.setMnemonicAt(1, java.awt.event.KeyEvent.VK_B);
        tabs.setMnemonicAt(2, java.awt.event.KeyEvent.VK_A);
        tabs.setMnemonicAt(3, java.awt.event.KeyEvent.VK_S);

        add(tabs, BorderLayout.CENTER);

        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(statusBar, BorderLayout.SOUTH);

        // Ensure data is saved on normal exit too.
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveData();
            }
        });
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(java.awt.event.KeyEvent.VK_F);

        JMenuItem saveItem = new JMenuItem("Save Data", java.awt.event.KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
        saveItem.addActionListener(e -> saveData());

        JMenuItem loadItem = new JMenuItem("Reload Data", java.awt.event.KeyEvent.VK_R);
        loadItem.addActionListener(e -> { loadInitialData(); refreshAll(); setStatus("Data reloaded from disk."); });

        JMenuItem exitItem = new JMenuItem("Exit", java.awt.event.KeyEvent.VK_X);
        exitItem.addActionListener(e -> { saveData(); System.exit(0); });

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(java.awt.event.KeyEvent.VK_E);
        JMenuItem undoItem = new JMenuItem("Undo Last Admin Action", java.awt.event.KeyEvent.VK_U);
        undoItem.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        undoItem.addActionListener(e -> {
            String result = manager.undo();
            setStatus(result);
            refreshAll();
        });
        editMenu.add(undoItem);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(java.awt.event.KeyEvent.VK_H);
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Smart Library Circulation & Automation System\nCOS 202 Project",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private void loadInitialData() {
        try {
            java.io.File f = new java.io.File(DEFAULT_DATA_FILE);
            if (f.exists()) {
                FileHandler.loadDatabase(db, DEFAULT_DATA_FILE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not load saved data: " + ex.getMessage(),
                    "Load Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void saveData() {
        try {
            new java.io.File("data").mkdirs();
            FileHandler.saveDatabase(db, DEFAULT_DATA_FILE);
            setStatus("Data saved to " + DEFAULT_DATA_FILE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save data: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setStatus(String message) {
        statusBar.setText(" " + message);
    }

    public void refreshAll() {
        viewItemsPanel.refresh();
        borrowPanel.refresh();
        adminPanel.refresh();
        searchSortPanel.refresh();
    }

    public LibraryManager getManager() { return manager; }
    public LibraryDatabase getDatabase() { return db; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
            new MainWindow().setVisible(true);
        });
    }
}

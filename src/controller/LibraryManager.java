package controller;

import model.LibraryDatabase;
import model.LibraryItem;
import model.UserAccount;
import utils.IDGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Top-level facade the GUI talks to for admin-style operations: adding
 * items, deleting items, undoing the last action, and producing the
 * required simple reports.
 */
public class LibraryManager {

    private final LibraryDatabase db;

    public LibraryManager(LibraryDatabase db) {
        this.db = db;
    }

    public LibraryDatabase getDatabase() { return db; }

    public void addItem(LibraryItem item) {
        db.addItem(item);
        db.refreshFrequentCache();
    }

    public String deleteItem(String itemId) {
        LibraryItem removed = db.removeItem(itemId);
        db.refreshFrequentCache();
        return removed == null ? "Item not found: " + itemId : "Deleted '" + removed.getTitle() + "'";
    }

    public String undo() {
        String result = db.undoLastAction();
        db.refreshFrequentCache();
        return result;
    }

    public UserAccount registerUser(String name) {
        UserAccount user = new UserAccount(IDGenerator.nextUserId(), name);
        db.addUser(user);
        return user;
    }

    // ---------------- polymorphism demo ----------------

    /**
     * Processes any LibraryItem regardless of concrete subtype - a single
     * method operating on the abstract base type, illustrating polymorphism.
     */
    public static String describeAny(LibraryItem item) {
        return item.displayInfo();
    }

    // ---------------- reports ----------------

    /** Report 1: most borrowed items, using the access-count cache. */
    public List<LibraryItem> mostBorrowedItemsReport() {
        db.refreshFrequentCache();
        List<LibraryItem> result = new ArrayList<>();
        for (LibraryItem item : db.getFrequentCache()) {
            if (item != null) result.add(item);
        }
        return result;
    }

    /** Report 2: users who currently have at least one overdue item. */
    public List<UserAccount> overdueUsersReport() {
        List<UserAccount> result = new ArrayList<>();
        for (UserAccount u : db.getUsers().values()) {
            if (u.hasOverdueItems()) result.add(u);
        }
        return result;
    }

    /** Report 3: category distribution, computed recursively. */
    public Map<String, Integer> categoryDistributionReport() {
        return db.countByCategoryRecursive();
    }
}

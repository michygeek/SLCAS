package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a library patron. Composition: a LibraryDatabase holds a
 * collection of UserAccount objects; each UserAccount holds its own
 * borrowing history rather than the item objects themselves (loose coupling
 * via ids).
 */
public class UserAccount implements Serializable {

    private final String userId;
    private String name;

    // itemId -> dueDate, for everything currently checked out
    private final Map<String, LocalDate> currentlyBorrowed = new LinkedHashMap<>();

    // full history of item ids ever borrowed (most recent last)
    private final List<String> borrowingHistory = new ArrayList<>();

    public UserAccount(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public void recordBorrow(String itemId, LocalDate dueDate) {
        currentlyBorrowed.put(itemId, dueDate);
        borrowingHistory.add(itemId);
    }

    public void recordReturn(String itemId) {
        currentlyBorrowed.remove(itemId);
    }

    /** Used only by FileHandler when restoring state from disk (skips history append). */
    public void restoreCurrentlyBorrowed(String itemId, LocalDate dueDate) {
        currentlyBorrowed.put(itemId, dueDate);
    }

    public Map<String, LocalDate> getCurrentlyBorrowed() { return currentlyBorrowed; }
    public List<String> getBorrowingHistory() { return borrowingHistory; }

    public boolean hasOverdueItems() {
        LocalDate today = LocalDate.now();
        for (LocalDate due : currentlyBorrowed.values()) {
            if (due.isBefore(today)) return true;
        }
        return false;
    }

    public List<String> getOverdueItemIds() {
        LocalDate today = LocalDate.now();
        List<String> overdue = new ArrayList<>();
        for (Map.Entry<String, LocalDate> e : currentlyBorrowed.entrySet()) {
            if (e.getValue().isBefore(today)) overdue.add(e.getKey());
        }
        return overdue;
    }

    @Override
    public String toString() {
        return userId + " - " + name + " (" + currentlyBorrowed.size() + " on loan)";
    }
}

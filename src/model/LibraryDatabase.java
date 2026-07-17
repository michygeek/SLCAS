package model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Encapsulates all in-memory data for the system:
 *  - ArrayList of LibraryItem (required by spec)
 *  - Map of UserAccount keyed by userId
 *  - a fixed-size Array used as a "most frequently accessed" cache
 *  - a Stack of AdminAction used to undo the last destructive admin action
 *
 * This class demonstrates composition: LibraryDatabase "has-a" collection of
 * items and users rather than extending them.
 */
public class LibraryDatabase {

    public static final int FREQUENT_CACHE_SIZE = 5;

    private final ArrayList<LibraryItem> items = new ArrayList<>();
    private final Map<String, UserAccount> users = new LinkedHashMap<>();

    // Fixed-size array cache of the most frequently accessed items.
    // Slot 0 = most accessed. Rebuilt whenever access counts change.
    private final LibraryItem[] frequentCache = new LibraryItem[FREQUENT_CACHE_SIZE];

    private final Stack<AdminAction> undoStack = new Stack<>();

    // ---------------- item management ----------------

    public ArrayList<LibraryItem> getItems() { return items; }

    public void addItem(LibraryItem item) {
        items.add(item);
        undoStack.push(new AdminAction(AdminAction.Type.ADD_ITEM, item, items.size() - 1));
    }

    /** Removes an item by id, recording the action so it can be undone. */
    public LibraryItem removeItem(String id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(id)) {
                LibraryItem removed = items.remove(i);
                undoStack.push(new AdminAction(AdminAction.Type.DELETE_ITEM, removed, i));
                return removed;
            }
        }
        return null;
    }

    /** Reverses the most recent admin action (add or delete). */
    public String undoLastAction() {
        if (undoStack.isEmpty()) return "Nothing to undo.";
        AdminAction action = undoStack.pop();
        if (action.getType() == AdminAction.Type.DELETE_ITEM) {
            int idx = Math.min(action.getOriginalIndex(), items.size());
            items.add(idx, action.getItem());
            return "Undo: restored '" + action.getItem().getTitle() + "'";
        } else { // ADD_ITEM -> undo means remove it again
            items.remove(action.getItem());
            return "Undo: removed '" + action.getItem().getTitle() + "' (undid add)";
        }
    }

    public boolean hasUndoableAction() { return !undoStack.isEmpty(); }

    public LibraryItem findById(String id) {
        for (LibraryItem it : items) if (it.getId().equals(id)) return it;
        return null;
    }

    // ---------------- user management ----------------

    public Map<String, UserAccount> getUsers() { return users; }

    public void addUser(UserAccount user) { users.put(user.getUserId(), user); }

    public UserAccount findUser(String userId) { return users.get(userId); }

    // ---------------- frequency cache (fixed-size array) ----------------

    /** Recomputes the top-N frequently accessed items into the fixed array. */
    public void refreshFrequentCache() {
        List<LibraryItem> sorted = new ArrayList<>(items);
        sorted.sort((a, b) -> b.getAccessCount() - a.getAccessCount());
        for (int i = 0; i < FREQUENT_CACHE_SIZE; i++) {
            frequentCache[i] = (i < sorted.size() && sorted.get(i).getAccessCount() > 0)
                    ? sorted.get(i) : null;
        }
    }

    public LibraryItem[] getFrequentCache() { return frequentCache; }

    // ---------------- recursion: count items per category ----------------

    /**
     * Recursively counts how many items fall under each category.
     * Demonstrates recursion over the ArrayList instead of a plain loop.
     */
    public Map<String, Integer> countByCategoryRecursive() {
        Map<String, Integer> result = new LinkedHashMap<>();
        countByCategoryHelper(items, 0, result);
        return result;
    }

    private void countByCategoryHelper(List<LibraryItem> list, int index, Map<String, Integer> acc) {
        if (index >= list.size()) return; // base case
        String cat = list.get(index).getCategory();
        acc.merge(cat, 1, Integer::sum);
        countByCategoryHelper(list, index + 1, acc); // recursive step
    }
}

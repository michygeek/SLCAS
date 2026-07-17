package model;

/**
 * Represents a single undoable admin operation. Pushed onto a Stack in
 * LibraryDatabase so the last destructive action (e.g. deleting an item)
 * can be reversed.
 */
public class AdminAction {

    public enum Type { ADD_ITEM, DELETE_ITEM }

    private final Type type;
    private final LibraryItem item;
    private final int originalIndex; // position the item was removed from, for DELETE undo

    public AdminAction(Type type, LibraryItem item, int originalIndex) {
        this.type = type;
        this.item = item;
        this.originalIndex = originalIndex;
    }

    public Type getType() { return type; }
    public LibraryItem getItem() { return item; }
    public int getOriginalIndex() { return originalIndex; }

    @Override
    public String toString() {
        return type + " -> " + item.getTitle();
    }
}

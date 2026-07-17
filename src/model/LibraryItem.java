package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Abstract base class for everything the library circulates.
 * Holds the shared borrowing state so that Book / Magazine / Journal can all
 * implement {@link Borrowable} without duplicating logic.
 */
public abstract class LibraryItem implements Serializable {

    protected String id;
    protected String title;
    protected String author;
    protected int year;
    protected String category;

    // How many times this item has been accessed/borrowed - drives the
    // "Most Frequently Accessed Items" fixed-size array cache.
    protected int accessCount;

    // Shared borrowing state (used by subclasses through the Borrowable
    // interface implementations).
    protected boolean borrowed;
    protected String borrowedBy;
    protected LocalDate dueDate;
    protected final Queue<String> reservationQueue = new LinkedList<>();

    protected LibraryItem(String id, String title, String author, int year, String category) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.category = category;
        this.accessCount = 0;
        this.borrowed = false;
    }

    // ---- abstract contract every concrete item type must fulfil ----

    /** @return a short label such as "Book", "Magazine", "Journal" */
    public abstract String getType();

    /** @return a human readable multi-field summary used by the GUI */
    public abstract String displayInfo();

    // ---- shared getters / setters (encapsulation) ----

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getAccessCount() { return accessCount; }
    public void bumpAccessCount() { accessCount++; }

    public boolean isBorrowed() { return borrowed; }
    public String getBorrowedBy() { return borrowedBy; }
    public LocalDate getDueDate() { return dueDate; }
    public Queue<String> getReservationQueue() { return reservationQueue; }

    protected boolean doBorrow(String userId, int loanDays) {
        if (borrowed) {
            reservationQueue.add(userId);
            return false;
        }
        borrowed = true;
        borrowedBy = userId;
        dueDate = LocalDate.now().plusDays(loanDays);
        bumpAccessCount();
        return true;
    }

    protected String doReturn() {
        borrowed = false;
        borrowedBy = null;
        dueDate = null;
        if (!reservationQueue.isEmpty()) {
            String next = reservationQueue.poll();
            borrowed = true;
            borrowedBy = next;
            dueDate = LocalDate.now().plusDays(14);
            return next;
        }
        return null;
    }

    protected void addReservationInternal(String userId) {
        reservationQueue.add(userId);
    }

    // ---- used only by FileHandler when restoring state from disk ----

    public void setAccessCount(int count) { this.accessCount = count; }

    public void restoreBorrowState(boolean borrowed, String borrowedBy, LocalDate dueDate) {
        this.borrowed = borrowed;
        this.borrowedBy = borrowedBy;
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s, %d) - %s", getType(), title, author, year,
                borrowed ? "Borrowed by " + borrowedBy : "Available");
    }
}

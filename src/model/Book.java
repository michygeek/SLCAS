package model;

import java.time.LocalDate;
import java.util.Queue;

public class Book extends LibraryItem implements Borrowable {

    private String isbn;

    public Book(String id, String title, String author, int year, String category, String isbn) {
        super(id, title, author, year, category);
        this.isbn = isbn;
    }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    @Override
    public String getType() { return "Book"; }

    @Override
    public String displayInfo() {
        return String.format("Book: %s by %s (%d) | ISBN: %s | Category: %s | %s",
                title, author, year, isbn, category,
                borrowed ? "Borrowed by " + borrowedBy + " due " + dueDate : "Available");
    }

    @Override
    public boolean isBorrowed() { return borrowed; }

    @Override
    public boolean borrowItem(String userId, int loanDays) { return doBorrow(userId, loanDays); }

    @Override
    public String returnItem() { return doReturn(); }

    @Override
    public String getBorrowedBy() { return borrowedBy; }

    @Override
    public LocalDate getDueDate() { return dueDate; }

    @Override
    public Queue<String> getReservationQueue() { return reservationQueue; }

    @Override
    public void addReservation(String userId) { addReservationInternal(userId); }
}

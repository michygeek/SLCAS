package model;

import java.time.LocalDate;
import java.util.Queue;

public class Journal extends LibraryItem implements Borrowable {

    private String volume;
    private String issue;

    public Journal(String id, String title, String author, int year, String category,
                   String volume, String issue) {
        super(id, title, author, year, category);
        this.volume = volume;
        this.issue = issue;
    }

    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }
    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }

    @Override
    public String getType() { return "Journal"; }

    @Override
    public String displayInfo() {
        return String.format("Journal: %s by %s (%d) | Vol %s Issue %s | Category: %s | %s",
                title, author, year, volume, issue, category,
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

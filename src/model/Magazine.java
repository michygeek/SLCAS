package model;

import java.time.LocalDate;
import java.util.Queue;

public class Magazine extends LibraryItem implements Borrowable {

    private String issueNumber;

    public Magazine(String id, String title, String author, int year, String category, String issueNumber) {
        super(id, title, author, year, category);
        this.issueNumber = issueNumber;
    }

    public String getIssueNumber() { return issueNumber; }
    public void setIssueNumber(String issueNumber) { this.issueNumber = issueNumber; }

    @Override
    public String getType() { return "Magazine"; }

    @Override
    public String displayInfo() {
        return String.format("Magazine: %s (%d) | Issue: %s | Category: %s | %s",
                title, year, issueNumber, category,
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

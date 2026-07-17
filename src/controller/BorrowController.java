package controller;

import model.Borrowable;
import model.LibraryDatabase;
import model.LibraryItem;
import model.UserAccount;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Handles the borrow/return workflow, including the reservation (waitlist)
 * queue, and computes overdue fines.
 */
public final class BorrowController {

    private static final int LOAN_DAYS = 14;
    private static final double DAILY_FINE = 50.0; // currency units per overdue day

    private final LibraryDatabase db;

    public BorrowController(LibraryDatabase db) {
        this.db = db;
    }

    /** @return a human-readable status message describing what happened */
    public String borrow(String itemId, String userId) {
        LibraryItem item = db.findById(itemId);
        UserAccount user = db.findUser(userId);
        if (item == null) return "No such item: " + itemId;
        if (user == null) return "No such user: " + userId;
        if (!(item instanceof Borrowable)) return item.getTitle() + " cannot be borrowed.";

        Borrowable b = (Borrowable) item;
        boolean gotItNow = b.borrowItem(userId, LOAN_DAYS);
        db.refreshFrequentCache();
        if (gotItNow) {
            user.recordBorrow(itemId, b.getDueDate());
            return user.getName() + " borrowed '" + item.getTitle() + "', due " + b.getDueDate();
        } else {
            return item.getTitle() + " is currently on loan. " + user.getName() +
                    " was added to the reservation queue (position " + b.getReservationQueue().size() + ").";
        }
    }

    /** @return a human-readable status message describing what happened */
    public String returnItem(String itemId) {
        LibraryItem item = db.findById(itemId);
        if (item == null) return "No such item: " + itemId;
        if (!(item instanceof Borrowable)) return item.getTitle() + " is not a borrowable item.";

        Borrowable b = (Borrowable) item;
        String previousBorrower = item.getBorrowedBy();
        double fine = previousBorrower != null ? computeOverdueChargeRecursive(daysOverdue(item.getDueDate())) : 0.0;

        String nextUserId = b.returnItem();

        if (previousBorrower != null) {
            UserAccount prevUser = db.findUser(previousBorrower);
            if (prevUser != null) prevUser.recordReturn(itemId);
        }

        StringBuilder msg = new StringBuilder();
        msg.append("'").append(item.getTitle()).append("' returned.");
        if (fine > 0) msg.append(" Overdue fine charged: ").append(fine);

        if (nextUserId != null) {
            UserAccount nextUser = db.findUser(nextUserId);
            if (nextUser != null) {
                nextUser.recordBorrow(itemId, item.getDueDate());
                msg.append(" Automatically handed to next reservation: ").append(nextUser.getName())
                        .append(", due ").append(item.getDueDate()).append(".");
            }
        }
        return msg.toString();
    }

    private int daysOverdue(LocalDate dueDate) {
        if (dueDate == null) return 0;
        long days = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        return (int) Math.max(0, days);
    }

    /**
     * Recursively computes the overdue fine: DAILY_FINE per day overdue.
     * Written recursively (rather than fine = days * DAILY_FINE) to satisfy
     * the "recursive overdue charge computation" requirement.
     */
    public double computeOverdueChargeRecursive(int daysOverdue) {
        if (daysOverdue <= 0) return 0.0; // base case
        return DAILY_FINE + computeOverdueChargeRecursive(daysOverdue - 1); // recursive step
    }
}

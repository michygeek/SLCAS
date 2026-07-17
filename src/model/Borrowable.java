package model;

import java.time.LocalDate;
import java.util.Queue;

/**
 * Interface implemented by any LibraryItem subtype that can be borrowed.
 * Demonstrates interface-based polymorphism separate from the LibraryItem
 * abstract class hierarchy.
 */
public interface Borrowable {

    /** @return true if the item is currently checked out to a user */
    boolean isBorrowed();

    /**
     * Attempt to borrow the item for the given user.
     * If the item is already borrowed, the user is added to the reservation
     * (waitlist) queue instead of being given the item immediately.
     *
     * @param userId    the id of the user requesting the item
     * @param loanDays  number of days until the item is due
     * @return true if the item was handed to the user immediately,
     *         false if the user was placed on the waitlist
     */
    boolean borrowItem(String userId, int loanDays);

    /**
     * Return the item. If the reservation queue is not empty, the next
     * user in line automatically becomes the new borrower.
     *
     * @return the userId of the next person the item was auto-assigned to,
     *         or null if the item is now simply available.
     */
    String returnItem();

    /** @return userId currently holding the item, or null if available */
    String getBorrowedBy();

    /** @return the due date for the current borrower, or null if not borrowed */
    LocalDate getDueDate();

    /** @return the FIFO waitlist/reservation queue for this item */
    Queue<String> getReservationQueue();

    /** Add a user to the reservation queue explicitly */
    void addReservation(String userId);
}

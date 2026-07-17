package utils;

/**
 * Small utility for generating unique, human-friendly ids such as
 * B0001 (books/items) and U0001 (users). Not thread-safe, but this is a
 * single-user desktop app so a simple static counter is fine.
 */
public final class IDGenerator {

    private static int itemCounter = 0;
    private static int userCounter = 0;

    private IDGenerator() { }

    public static String nextItemId() {
        itemCounter++;
        return String.format("I%04d", itemCounter);
    }

    public static String nextUserId() {
        userCounter++;
        return String.format("U%04d", userCounter);
    }

    /** Lets FileHandler re-sync the counters after loading existing data. */
    public static void ensureItemCounterAtLeast(int value) {
        if (value > itemCounter) itemCounter = value;
    }

    public static void ensureUserCounterAtLeast(int value) {
        if (value > userCounter) userCounter = value;
    }
}

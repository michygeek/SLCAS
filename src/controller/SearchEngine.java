package controller;

import model.LibraryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Student-implemented search algorithms over a list of LibraryItem.
 * Field is one of "title", "author", "type".
 */
public final class SearchEngine {

    private SearchEngine() { }

    public enum Field { TITLE, AUTHOR, TYPE }

    private static String keyOf(LibraryItem item, Field field) {
        switch (field) {
            case AUTHOR: return item.getAuthor();
            case TYPE: return item.getType();
            case TITLE:
            default: return item.getTitle();
        }
    }

    /**
     * Linear search: O(n). Works regardless of whether the list is sorted,
     * and returns every match (a title/author can repeat).
     */
    public static List<LibraryItem> linearSearch(List<LibraryItem> items, String query, Field field) {
        List<LibraryItem> results = new ArrayList<>();
        String q = query.toLowerCase();
        for (LibraryItem item : items) {
            String key = keyOf(item, field);
            if (key != null && key.toLowerCase().contains(q)) {
                results.add(item);
            }
        }
        return results;
    }

    /**
     * Binary search: O(log n). Requires the list to already be sorted
     * ascending by the same field being searched, and matches on exact
     * (case-insensitive) equality rather than "contains", since binary
     * search needs an ordering comparison, not a substring test.
     * Returns the single item found, or null.
     */
    public static LibraryItem binarySearch(List<LibraryItem> sortedItems, String query, Field field) {
        int lo = 0, hi = sortedItems.size() - 1;
        String q = query.toLowerCase();
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            String key = keyOf(sortedItems.get(mid), field);
            int cmp = key.toLowerCase().compareTo(q);
            if (cmp == 0) return sortedItems.get(mid);
            else if (cmp < 0) lo = mid + 1;
            else hi = mid - 1;
        }
        return null;
    }

    /**
     * Recursive linear search: same semantics as linearSearch (substring,
     * every match) but implemented recursively instead of with a loop, to
     * satisfy the "recursive component" requirement.
     */
    public static List<LibraryItem> recursiveSearch(List<LibraryItem> items, String query, Field field) {
        List<LibraryItem> results = new ArrayList<>();
        recursiveSearchHelper(items, query.toLowerCase(), field, 0, results);
        return results;
    }

    private static void recursiveSearchHelper(List<LibraryItem> items, String query, Field field,
                                               int index, List<LibraryItem> acc) {
        if (index >= items.size()) return; // base case
        LibraryItem item = items.get(index);
        String key = keyOf(item, field);
        if (key != null && key.toLowerCase().contains(query)) {
            acc.add(item);
        }
        recursiveSearchHelper(items, query, field, index + 1, acc); // recursive step
    }
}

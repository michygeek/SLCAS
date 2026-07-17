package controller;

import model.LibraryItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Student-implemented sorting algorithms over a list of LibraryItem.
 * Each method returns a new sorted ArrayList and leaves the input untouched,
 * so the GUI can freely compare "before/after" or re-sort by a different
 * field without losing the original order.
 */
public final class SortEngine {

    private SortEngine() { }

    public enum Field { TITLE, AUTHOR, YEAR }
    public enum Algorithm { SELECTION, INSERTION, MERGE, QUICK }

    private static Comparator<LibraryItem> comparatorFor(Field field) {
        switch (field) {
            case AUTHOR: return Comparator.comparing(LibraryItem::getAuthor, String.CASE_INSENSITIVE_ORDER);
            case YEAR: return Comparator.comparingInt(LibraryItem::getYear);
            case TITLE:
            default: return Comparator.comparing(LibraryItem::getTitle, String.CASE_INSENSITIVE_ORDER);
        }
    }

    public static List<LibraryItem> sort(List<LibraryItem> items, Field field, Algorithm algorithm) {
        List<LibraryItem> copy = new ArrayList<>(items);
        Comparator<LibraryItem> cmp = comparatorFor(field);
        switch (algorithm) {
            case SELECTION: selectionSort(copy, cmp); break;
            case INSERTION: insertionSort(copy, cmp); break;
            case MERGE: mergeSort(copy, 0, copy.size() - 1, cmp); break;
            case QUICK: quickSort(copy, 0, copy.size() - 1, cmp); break;
        }
        return copy;
    }

    // ---------------- Selection Sort: O(n^2) ----------------
    private static void selectionSort(List<LibraryItem> list, Comparator<LibraryItem> cmp) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (cmp.compare(list.get(j), list.get(minIdx)) < 0) minIdx = j;
            }
            if (minIdx != i) {
                LibraryItem tmp = list.get(i);
                list.set(i, list.get(minIdx));
                list.set(minIdx, tmp);
            }
        }
    }

    // ---------------- Insertion Sort: O(n^2), fast on nearly-sorted data ----------------
    private static void insertionSort(List<LibraryItem> list, Comparator<LibraryItem> cmp) {
        for (int i = 1; i < list.size(); i++) {
            LibraryItem key = list.get(i);
            int j = i - 1;
            while (j >= 0 && cmp.compare(list.get(j), key) > 0) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }

    // ---------------- Merge Sort: O(n log n), stable ----------------
    private static void mergeSort(List<LibraryItem> list, int left, int right, Comparator<LibraryItem> cmp) {
        if (left >= right) return; // base case
        int mid = (left + right) / 2;
        mergeSort(list, left, mid, cmp);
        mergeSort(list, mid + 1, right, cmp);
        merge(list, left, mid, right, cmp);
    }

    private static void merge(List<LibraryItem> list, int left, int mid, int right, Comparator<LibraryItem> cmp) {
        List<LibraryItem> leftPart = new ArrayList<>(list.subList(left, mid + 1));
        List<LibraryItem> rightPart = new ArrayList<>(list.subList(mid + 1, right + 1));

        int i = 0, j = 0, k = left;
        while (i < leftPart.size() && j < rightPart.size()) {
            if (cmp.compare(leftPart.get(i), rightPart.get(j)) <= 0) {
                list.set(k++, leftPart.get(i++));
            } else {
                list.set(k++, rightPart.get(j++));
            }
        }
        while (i < leftPart.size()) list.set(k++, leftPart.get(i++));
        while (j < rightPart.size()) list.set(k++, rightPart.get(j++));
    }

    // ---------------- Quick Sort: O(n log n) average, in place ----------------
    private static void quickSort(List<LibraryItem> list, int low, int high, Comparator<LibraryItem> cmp) {
        if (low >= high) return; // base case
        int pivotIndex = partition(list, low, high, cmp);
        quickSort(list, low, pivotIndex - 1, cmp);
        quickSort(list, pivotIndex + 1, high, cmp);
    }

    private static int partition(List<LibraryItem> list, int low, int high, Comparator<LibraryItem> cmp) {
        LibraryItem pivot = list.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (cmp.compare(list.get(j), pivot) <= 0) {
                i++;
                LibraryItem tmp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, tmp);
            }
        }
        LibraryItem tmp = list.get(i + 1);
        list.set(i + 1, list.get(high));
        list.set(high, tmp);
        return i + 1;
    }
}

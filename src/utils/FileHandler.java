package utils;

import model.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles saving/loading the whole LibraryDatabase (items + users) to a
 * single JSON file on disk, using the small hand-rolled {@link SimpleJson}
 * reader/writer so the project has no external dependencies.
 */
public final class FileHandler {

    private FileHandler() { }

    // ----------------------------- SAVE -----------------------------

    public static void saveDatabase(LibraryDatabase db, String path) throws IOException {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("items", itemsToJson(db.getItems()));
        root.put("users", usersToJson(db.getUsers().values()));

        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(Path.of(path), StandardCharsets.UTF_8))) {
            out.print(SimpleJson.write(root));
        }
    }

    private static List<Object> itemsToJson(List<LibraryItem> items) {
        List<Object> list = new ArrayList<>();
        for (LibraryItem it : items) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id", it.getId());
            o.put("type", it.getType());
            o.put("title", it.getTitle());
            o.put("author", it.getAuthor());
            o.put("year", (double) it.getYear());
            o.put("category", it.getCategory());
            o.put("accessCount", (double) it.getAccessCount());
            o.put("borrowed", it.isBorrowed());
            o.put("borrowedBy", it.getBorrowedBy());
            o.put("dueDate", it.getDueDate() == null ? null : it.getDueDate().toString());
            o.put("reservationQueue", new ArrayList<Object>(it.getReservationQueue()));

            if (it instanceof Book) {
                o.put("isbn", ((Book) it).getIsbn());
            } else if (it instanceof Magazine) {
                o.put("issueNumber", ((Magazine) it).getIssueNumber());
            } else if (it instanceof Journal) {
                o.put("volume", ((Journal) it).getVolume());
                o.put("issue", ((Journal) it).getIssue());
            }
            list.add(o);
        }
        return list;
    }

    private static List<Object> usersToJson(Iterable<UserAccount> users) {
        List<Object> list = new ArrayList<>();
        for (UserAccount u : users) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("userId", u.getUserId());
            o.put("name", u.getName());

            Map<String, Object> borrowed = new LinkedHashMap<>();
            for (Map.Entry<String, LocalDate> e : u.getCurrentlyBorrowed().entrySet()) {
                borrowed.put(e.getKey(), e.getValue().toString());
            }
            o.put("currentlyBorrowed", borrowed);
            o.put("borrowingHistory", new ArrayList<Object>(u.getBorrowingHistory()));
            list.add(o);
        }
        return list;
    }

    // ----------------------------- LOAD -----------------------------

    @SuppressWarnings("unchecked")
    public static void loadDatabase(LibraryDatabase db, String path) throws IOException {
        String text = Files.readString(Path.of(path), StandardCharsets.UTF_8);
        Object parsed = SimpleJson.parse(text);
        if (!(parsed instanceof Map)) return;
        Map<String, Object> root = (Map<String, Object>) parsed;

        db.getItems().clear();
        db.getUsers().clear();

        List<Object> itemsJson = (List<Object>) root.getOrDefault("items", new ArrayList<>());
        int maxItemNum = 0;
        for (Object o : itemsJson) {
            Map<String, Object> m = (Map<String, Object>) o;
            LibraryItem item = itemFromJson(m);
            db.getItems().add(item);
            maxItemNum = Math.max(maxItemNum, numericSuffix(item.getId()));
        }
        IDGenerator.ensureItemCounterAtLeast(maxItemNum);

        List<Object> usersJson = (List<Object>) root.getOrDefault("users", new ArrayList<>());
        int maxUserNum = 0;
        for (Object o : usersJson) {
            Map<String, Object> m = (Map<String, Object>) o;
            UserAccount user = userFromJson(m);
            db.addUser(user);
            maxUserNum = Math.max(maxUserNum, numericSuffix(user.getUserId()));
        }
        IDGenerator.ensureUserCounterAtLeast(maxUserNum);

        db.refreshFrequentCache();
    }

    private static int numericSuffix(String id) {
        try {
            return Integer.parseInt(id.replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    private static LibraryItem itemFromJson(Map<String, Object> m) {
        String id = (String) m.get("id");
        String type = (String) m.get("type");
        String title = (String) m.get("title");
        String author = (String) m.get("author");
        int year = ((Double) m.get("year")).intValue();
        String category = (String) m.get("category");

        LibraryItem item;
        switch (type) {
            case "Magazine":
                item = new Magazine(id, title, author, year, category, (String) m.getOrDefault("issueNumber", ""));
                break;
            case "Journal":
                item = new Journal(id, title, author, year, category,
                        (String) m.getOrDefault("volume", ""), (String) m.getOrDefault("issue", ""));
                break;
            case "Book":
            default:
                item = new Book(id, title, author, year, category, (String) m.getOrDefault("isbn", ""));
                break;
        }

        item.setAccessCount(((Double) m.getOrDefault("accessCount", 0.0)).intValue());
        boolean borrowed = Boolean.TRUE.equals(m.get("borrowed"));
        String borrowedBy = (String) m.get("borrowedBy");
        String due = (String) m.get("dueDate");
        LocalDate dueDate = due == null ? null : LocalDate.parse(due);
        item.restoreBorrowState(borrowed, borrowedBy, dueDate);

        List<Object> queue = (List<Object>) m.getOrDefault("reservationQueue", new ArrayList<>());
        for (Object u : queue) item.getReservationQueue().add((String) u);

        return item;
    }

    @SuppressWarnings("unchecked")
    private static UserAccount userFromJson(Map<String, Object> m) {
        String userId = (String) m.get("userId");
        String name = (String) m.get("name");
        UserAccount user = new UserAccount(userId, name);

        Map<String, Object> borrowed = (Map<String, Object>) m.getOrDefault("currentlyBorrowed", new LinkedHashMap<>());
        for (Map.Entry<String, Object> e : borrowed.entrySet()) {
            user.restoreCurrentlyBorrowed(e.getKey(), LocalDate.parse((String) e.getValue()));
        }
        List<Object> history = (List<Object>) m.getOrDefault("borrowingHistory", new ArrayList<>());
        for (Object h : history) user.getBorrowingHistory().add((String) h);

        return user;
    }
}

# Smart Library Circulation & Automation System (SLCAS)
COS 202 Project

A Java Swing desktop application for managing a university library's
catalogue, borrowing/returning workflow, reservations, admin operations, and
reporting.

## How to build & run

Requires a JDK (11+; developed against JDK 21). No external libraries are
needed - everything, including JSON persistence, is hand-rolled in `utils/`.

```bash
# from the SLCAS/ folder
./run.sh
```

That script simply does:

```bash
mkdir -p bin
javac -d bin $(find src -name "*.java")
java -cp bin Main
```

If you prefer an IDE (IntelliJ / Eclipse / VS Code), just open the `src`
folder as the source root and run `Main.java`.

Data is auto-loaded from `data/library_data.json` on startup (if present)
and auto-saved there on exit, or any time you choose **File > Save Data**.

## Project structure

```
src/
  Main.java                 entry point
  model/
    LibraryItem.java         abstract base class
    Borrowable.java           interface (borrow/return/reservation contract)
    Book.java, Magazine.java, Journal.java   concrete subclasses
    UserAccount.java          patron + borrowing history
    LibraryDatabase.java       ArrayList of items, Map of users,
                               fixed-size array cache, undo Stack
    AdminAction.java           record used by the undo stack
  controller/
    LibraryManager.java        add/delete/undo, reports, polymorphism demo
    SearchEngine.java           linear / binary / recursive search
    SortEngine.java             selection / insertion / merge / quick sort
    BorrowController.java       borrow/return workflow, recursive overdue fine
  gui/
    MainWindow.java             JFrame, menu bar, tabs, status bar
    ViewItemsPanel.java         JTable + custom renderer
    BorrowPanel.java            borrow/return form + Timer-based reminders
    AdminPanel.java             CardLayout screens, file chooser, undo
    SearchSortPanel.java        GridBagLayout search/sort controls
    ItemTableModel.java         shared table model
  utils/
    IDGenerator.java            unique id generation
    FileHandler.java            JSON save/load
    SimpleJson.java             minimal dependency-free JSON reader/writer
```

## Requirement checklist

| Requirement | Where |
|---|---|
| Abstract class + subclasses + interface | `model/LibraryItem.java`, `Book/Magazine/Journal.java`, `Borrowable.java` |
| Polymorphism over any LibraryItem | `LibraryManager.describeAny()` |
| Encapsulation & composition | `LibraryDatabase` (has-a items/users), `UserAccount` (has-a history) |
| ArrayList | `LibraryDatabase.items` |
| Queue (reservation/waitlist) | `LibraryItem.reservationQueue` (one per item) |
| Stack (undo) | `LibraryDatabase.undoStack` |
| Fixed-size array cache | `LibraryDatabase.frequentCache` |
| Linear / Binary / Recursive search | `SearchEngine.java` |
| Selection / Insertion / Merge / Quick sort | `SortEngine.java` |
| Recursion | `SearchEngine.recursiveSearch`, `LibraryDatabase.countByCategoryRecursive`, `BorrowController.computeOverdueChargeRecursive` |
| Event-driven GUI | button/menu/combo listeners throughout `gui/`, `Timer` in `BorrowPanel` |
| Tabbed panels: View / Borrow / Admin / Search&Sort | `MainWindow` (`JTabbedPane`) |
| Tables, buttons, text fields, combos, labels, dialogs, status bar | throughout `gui/` |
| BorderLayout, GridBagLayout, CardLayout | `MainWindow`/panels (Border), `SearchSortPanel` (GridBag), `AdminPanel` (Card, x2) |
| Custom renderer | `ViewItemsPanel.StatusRowRenderer` |
| Dynamic components at runtime | `AdminPanel` extra-field CardLayout switches per item type |
| File chooser import/export | `AdminPanel` Import/Export buttons |
| Timer for overdue notifications | `BorrowPanel.startOverdueTimer` |
| Input validation dialogs | `AdminPanel.doAddItem`, `BorrowPanel` |
| Mnemonics / keyboard shortcuts | menu items, tab mnemonics, button mnemonics |
| Tooltips | most buttons |
| Persistence (JSON) | `utils/FileHandler.java` + `utils/SimpleJson.java` |
| Reports: most borrowed, overdue users, category distribution | `LibraryManager` report methods, shown in `AdminPanel` Reports card |

## Notes on the JSON persistence

The assignment calls for text/JSON file persistence. Rather than depend on
an external library (e.g. `org.json`, Jackson, Gson) that would need to be
downloaded and added to the classpath, `utils/SimpleJson.java` implements a
small, dependency-free JSON reader/writer sufficient for this app's data
shapes (objects, arrays, strings, numbers, booleans, null). This keeps the
project buildable with nothing but the standard JDK.

## A note on this build environment

This code was written and carefully reviewed in an environment without a
JDK available to compile it directly (only a JRE was present, and package
installation requires network access that wasn't available). It has been
checked line-by-line for correctness, but please compile it in your own
environment as a first step, and let me know if you hit any compiler errors -
I can fix them immediately.

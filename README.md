# LibraryManagement

Simple Swing-based library manager with readers, books, and borrow/return slips stored under `data/`.

## Run

```bash
mvn -q -DskipTests package
java -cp target/LibraryManagement-1.0-SNAPSHOT.jar org.example.Main
```

## Borrow/Return

Use the **Borrow** tab to create borrow slips and return them. The app updates `data/borrows.csv` and book availability in `data/books.csv`.

The app now stores all persisted data in CSV files (`books.csv`, `readers.csv`, `borrows.csv`, `accounts.csv`). If legacy `.txt` files are present, they are loaded once and migrated automatically.


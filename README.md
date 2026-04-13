# LibraryManagement

Simple Swing-based library manager with readers, books, and borrow/return slips stored under `data/`.

## Run

```bash
mvn -q -DskipTests package
java -cp target/LibraryManagement-1.0-SNAPSHOT.jar org.example.Main
```

## Borrow/Return

Use the **Borrow** tab to create borrow slips and return them. The app updates `data/borrows.txt` and book availability in `data/books.txt`.


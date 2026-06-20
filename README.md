# LibraryManagement
Simple Swing-based library manager with readers, books, and borrow/return slips stored under `data/`.

# Features
- Create, delete, update books.
- Create, delete, update borrowers.
- Create, delete, update borrowslips.
- Export borrowslips.
- Statistics.

## Run
```bash
mvn -q -DskipTests package
java -cp target/LibraryManagement-1.0-SNAPSHOT.jar org.example.Main
```


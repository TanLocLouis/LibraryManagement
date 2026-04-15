package org.example.dao;

import org.example.model.Book;
import org.example.util.CsvUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDAO {
    private static final Path FILE_PATH = Paths.get("data", "books.csv");
    private static final Path LEGACY_FILE_PATH = Paths.get("data", "books.txt");
    private final ArrayList<Book> books = new ArrayList<>();

    public BookDAO() {
        loadBooks();
    }

    // Load and Save
    public void loadBooks() {
        books.clear();
        if (Files.exists(FILE_PATH)) {
            loadFromCsv(FILE_PATH);
            return;
        }
        if (Files.exists(LEGACY_FILE_PATH)) {
            loadFromLegacyTxt(LEGACY_FILE_PATH);
            saveBooks();
        }
    }

    private void loadFromCsv(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> parts = CsvUtil.parseRow(line);
                if (parts.size() < 8) {
                    continue;
                }
                books.add(new Book(
                        parts.get(0),
                        parts.get(1),
                        parts.get(2),
                        parts.get(3),
                        parts.get(4),
                        parseInt(parts.get(5)),
                        parseInt(parts.get(6)),
                        parseInt(parts.get(7))
                ));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load books", e);
        }
    }

    private void loadFromLegacyTxt(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length < 8) {
                    continue;
                }
                books.add(new Book(
                        parts[0],
                        parts[1],
                        parts[2],
                        parts[3],
                        parts[4],
                        parseInt(parts[5]),
                        parseInt(parts[6]),
                        parseInt(parts[7])
                ));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load books", e);
        }
    }

    public void saveBooks() {
        try {
            Path parent = FILE_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(FILE_PATH)) {
                for (Book book : books) {
                    writer.write(CsvUtil.formatRow(List.of(
                            safe(book.getIsbn()),
                            safe(book.getTitle()),
                            safe(book.getAuthor()),
                            safe(book.getCategory()),
                            safe(book.getPublisher()),
                            Integer.toString(book.getPublishYear()),
                            Integer.toString(book.getTotalCopies()),
                            Integer.toString(book.getAvailableCopies()))));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save books", e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    // Getters and Setters
    public void addBook(Book book) {
        books.add(book);
    }

    public ArrayList<Book> getBooks() {
        return books;
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public Optional<Book> findByIsbn(String isbn) {
        if (isbn == null) {
            return Optional.empty();
        }
        String normalized = isbn.trim();
        return books.stream()
                .filter(book -> book.getIsbn() != null && book.getIsbn().trim().equals(normalized))
                .findFirst();
    }

    public boolean deleteByIsbn(String isbn) {
        if (isbn == null) {
            return false;
        }
        String normalized = isbn.trim();
        return books.removeIf(book -> normalized.equals(book.getIsbn()));
    }

    public boolean updateByIsbn(String isbn, Book updatedBook) {
        if (isbn == null || updatedBook == null) {
            return false;
        }
        for (int i = 0; i < books.size(); i++) {
            Book current = books.get(i);
            if (current.getIsbn() != null && current.getIsbn().equals(isbn)) {
                books.set(i, updatedBook);
                return true;
            }
        }
        return false;
    }

    // Utils functions
    public int countBooks() {
        return books.size();
    }

    public int countBooksByCategory(String category) {
        if (category == null) {
            return 0;
        }
        String normalized = category.trim().toLowerCase();
        return (int) books.stream()
                .filter(book -> book.getCategory() != null
                        && book.getCategory().toLowerCase().equals(normalized))
                .count();
    }
}

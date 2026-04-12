package org.example.dao;

import org.example.model.Book;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

public class BookDAO {
    private static final Path FILE_PATH = Paths.get("data", "books.txt");
    private final ArrayList<Book> books = new ArrayList<>();

    public BookDAO() {
        loadBooks();
    }

    // Load and Save
    public void loadBooks() {
        books.clear();
        if (!Files.exists(FILE_PATH)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(FILE_PATH)) {
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
                    writer.write(String.join("|",
                            safe(book.getIsbn()),
                            safe(book.getTitle()),
                            safe(book.getAuthor()),
                            safe(book.getCategory()),
                            safe(book.getPublisher()),
                            Integer.toString(book.getPublishYear()),
                            Integer.toString(book.getTotalCopies()),
                            Integer.toString(book.getAvailableCopies())));
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
        return books.stream()
                .filter(book -> book.getIsbn() != null && book.getIsbn().equals(isbn))
                .findFirst();
    }

    public boolean deleteByIsbn(String isbn) {
        if (isbn == null) {
            return false;
        }
        String normalized = isbn.trim();
        return books.removeIf(book -> normalized.equals(book.getIsbn()));
    }
}

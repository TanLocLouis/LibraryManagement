package org.example.service;

import org.example.dao.BookDAO;
import org.example.model.Book;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class BookService {
    private final BookDAO bookDAO;

    public BookService() {
        this(new BookDAO());
    }

    public BookService(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
    }

    public List<Book> searchByTitle(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return bookDAO.getBooks().stream()
                .filter(book -> book.getTitle() != null
                        && book.getTitle().toLowerCase(Locale.ROOT).contains(normalized))
                .collect(Collectors.toList());
    }

    public boolean validate(Book book) {
        return book != null
                && notBlank(book.getIsbn())
                && notBlank(book.getTitle())
                && book.getTotalCopies() >= 0
                && book.getAvailableCopies() >= 0;
    }

    public BookDAO getBookDAO() {
        return bookDAO;
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    //
    public void addBook(Book book) {
        if (validate(book)) {
            bookDAO.addBook(book);
        } else {
            throw new IllegalArgumentException("Invalid book data");
        }
    }

    public void saveBooks() {
        bookDAO.saveBooks();
    }

    public boolean deleteBook(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN cannot be blank");
        }
        boolean deleted = bookDAO.deleteByIsbn(isbn);
        if (deleted) {
            saveBooks();
        }
        return deleted;
    }

    public boolean updateBook(String isbn, Book updatedBook) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN cannot be blank");
        }
        if (!validate(updatedBook)) {
            throw new IllegalArgumentException("Invalid book data");
        }
        boolean updated = bookDAO.updateByIsbn(isbn, updatedBook);
        if (updated) {
            saveBooks();
        }
        return updated;
    }

    public List<Book> searchByIsbn(String isbn) {
        if (isbn == null) {
            return List.of();
        }
        return bookDAO.findByIsbn(isbn.trim()).map(List::of).orElse(List.of());
    }

    // Utils functions
    public int countBooks() {
        return bookDAO.countBooks();
    }

    public int countBooksByCategory(String category) {
        return bookDAO.countBooksByCategory(category);
    }
}

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
        if (bookDAO.getBooks().isEmpty()) {
            return;
        }
        bookDAO.saveBooks();
    }
}


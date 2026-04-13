package org.example.service;

import org.example.model.Book;
import org.example.model.BorrowSlip;
import org.example.model.Reader;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticService {
    private final BookService bookService;
    private final ReaderService readerService;
    private final BorrowService borrowService;

    public StatisticService() {
        this(new BookService(), new ReaderService(), new BorrowService());
    }

    public StatisticService(BookService bookService, ReaderService readerService, BorrowService borrowService) {
        this.bookService = bookService;
        this.readerService = readerService;
        this.borrowService = borrowService;
    }

    public int countReaders() {
        return readerService.getReaderDAO().countReaders();
    }

    public int countBorrowedBooks() {
        return borrowService.countBorrowSlips();
    }

    public int countTotalBooksInLibrary() {
        return bookService.countBooks();
    }

    public Map<String, Integer> countBooksByCategory() {
        Map<String, Integer> counts = new HashMap<>();
        Map<String, Book> bookByIsbn = new HashMap<>();
        for (Book book : bookService.getBookDAO().getBooks()) {
            bookByIsbn.put(book.getIsbn(), book);
            String category = normalizeLabel(book.getCategory(), "Unknown");
            counts.put(category, counts.getOrDefault(category, 0) + Math.max(0, book.getAvailableCopies()));
        }

        for (BorrowSlip slip : borrowService.getActiveBorrowSlips()) {
            if (slip.getIsbnList() == null) {
                continue;
            }
            for (String isbn : slip.getIsbnList()) {
                Book book = bookByIsbn.get(isbn);
                String category = book == null ? "Unknown" : normalizeLabel(book.getCategory(), "Unknown");
                counts.put(category, counts.getOrDefault(category, 0) + 1);
            }
        }

        return counts;
    }

    public Map<String, Integer> countReadersByGender() {
        Map<String, Integer> counts = new HashMap<>();
        for (Reader reader : readerService.getReaderDAO().getReaders()) {
            String gender = normalizeLabel(reader.getGender(), "Unknown");
            counts.put(gender, counts.getOrDefault(gender, 0) + 1);
        }
        return counts;
    }

    public List<OverdueEntry> getOverdueEntries() {
        List<OverdueEntry> entries = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (BorrowSlip slip : borrowService.getActiveBorrowSlips()) {
            if (slip.getDueDate() == null || slip.getDueDate().isBlank()) {
                continue;
            }
            try {
                LocalDate dueDate = LocalDate.parse(slip.getDueDate());
                if (dueDate.isBefore(today)) {
                    long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(dueDate, today);
                    Reader reader = readerService.getReaderDAO().findById(slip.getReaderId()).orElse(null);
                    entries.add(new OverdueEntry(reader, slip, overdueDays));
                }
            } catch (Exception ignored) {
                // Skip malformed dates.
            }
        }
        return entries;
    }

    private String normalizeLabel(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 1) {
            return trimmed.toUpperCase(Locale.ROOT);
        }
        return trimmed.substring(0, 1).toUpperCase(Locale.ROOT) + trimmed.substring(1);
    }

    public static final class OverdueEntry {
        private final Reader reader;
        private final BorrowSlip slip;
        private final long overdueDays;

        public OverdueEntry(Reader reader, BorrowSlip slip, long overdueDays) {
            this.reader = reader;
            this.slip = slip;
            this.overdueDays = overdueDays;
        }

        public Reader getReader() {
            return reader;
        }

        public BorrowSlip getSlip() {
            return slip;
        }

        public long getOverdueDays() {
            return overdueDays;
        }
    }
}

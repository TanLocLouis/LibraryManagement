package org.example.service;

import org.example.dao.BorrowDAO;
import org.example.model.Book;
import org.example.model.BorrowSlip;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class BorrowService {
    public static final int MAX_BORROW_DAYS = 7;
    public static final long OVERDUE_FINE_PER_DAY = 5_000L;
    public static final int LOST_BOOK_FINE_MULTIPLIER = 2;

    private final BorrowDAO borrowDAO;
    private final BookService bookService;
    private final ReaderService readerService;

    public record PenaltyBreakdown(int overdueDays, long overdueFine, long lostBookFine, long totalFine) {
    }

    public BorrowService() {
        this(new BorrowDAO(), new BookService(), new ReaderService());
    }

    public BorrowService(BorrowDAO borrowDAO) {
        this(borrowDAO, new BookService(), new ReaderService());
    }

    public BorrowService(BookService bookService, ReaderService readerService) {
        this(new BorrowDAO(), bookService, readerService);
    }

    public BorrowService(BorrowDAO borrowDAO, BookService bookService, ReaderService readerService) {
        this.borrowDAO = borrowDAO;
        this.bookService = bookService;
        this.readerService = readerService;
    }

    public double calculateFine(BorrowSlip borrowSlip, LocalDate currentDate, double dailyFine) {
        if (borrowSlip == null || borrowSlip.getDueDate() == null || currentDate == null || dailyFine < 0) {
            return 0.0;
        }
        try {
            LocalDate dueDate = LocalDate.parse(borrowSlip.getDueDate());
            long overdueDays = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(dueDate, currentDate));
            return overdueDays * dailyFine;
        } catch (DateTimeParseException e) {
            return 0.0;
        }
    }

    public boolean validate(BorrowSlip borrowSlip) {
        return borrowSlip != null
                && borrowSlip.getSlipId() != null
                && !borrowSlip.getSlipId().isBlank()
                && borrowSlip.getReaderId() != null
                && !borrowSlip.getReaderId().isBlank()
                && borrowSlip.getIsbnList() != null
                && !borrowSlip.getIsbnList().isEmpty();
    }

    public BorrowSlip createBorrowSlip(String slipId, String readerId, LocalDate borrowDate, LocalDate dueDate, List<String> isbnList) {
        String normalizedSlipId = (slipId == null || slipId.isBlank()) ? UUID.randomUUID().toString() : slipId.trim();
        String normalizedReaderId = readerId == null ? "" : readerId.trim();
        if (normalizedReaderId.isBlank()) {
            throw new IllegalArgumentException("Reader ID is required");
        }
        if (borrowDate == null || dueDate == null) {
            throw new IllegalArgumentException("Borrow date and due date are required");
        }
        if (dueDate.isBefore(borrowDate)) {
            throw new IllegalArgumentException("Due date must be on or after borrow date");
        }
        if (dueDate.isAfter(borrowDate.plusDays(MAX_BORROW_DAYS))) {
            throw new IllegalArgumentException("A book can be borrowed for at most " + MAX_BORROW_DAYS + " days");
        }
        List<String> normalizedIsbnList = normalizeIsbnList(isbnList);
        if (normalizedIsbnList.isEmpty()) {
            throw new IllegalArgumentException("ISBN list is required");
        }
        if (borrowDAO.findById(normalizedSlipId).isPresent()) {
            throw new IllegalArgumentException("Slip ID already exists");
        }
        if (readerService.getReaderDAO().findById(normalizedReaderId).isEmpty()) {
            throw new IllegalArgumentException("Reader not found");
        }

        Map<String, Integer> counts = countIsbns(normalizedIsbnList);
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            Book book = bookService.getBookDAO().findByIsbn(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Book not found: " + entry.getKey()));
            if (book.getAvailableCopies() < entry.getValue()) {
                throw new IllegalArgumentException("Not enough copies for ISBN: " + entry.getKey());
            }
        }

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            Book book = bookService.getBookDAO().findByIsbn(entry.getKey()).orElseThrow();
            book.setAvailableCopies(book.getAvailableCopies() - entry.getValue());
        }
        bookService.saveBooks();

        BorrowSlip borrowSlip = new BorrowSlip(
                normalizedSlipId,
                normalizedReaderId,
                borrowDate.toString(),
                dueDate.toString(),
                null,
                normalizedIsbnList
        );
        borrowDAO.addBorrowSlip(borrowSlip);
        borrowDAO.saveBorrowSlips();
        return borrowSlip;
    }

    public BorrowSlip returnBorrowSlip(String slipId, LocalDate returnDate) {
        return returnBorrowSlip(slipId, returnDate, List.of());
    }

    public BorrowSlip returnBorrowSlip(String slipId, LocalDate returnDate, List<String> lostIsbnList) {
        String normalizedSlipId = slipId == null ? "" : slipId.trim();
        if (normalizedSlipId.isBlank()) {
            throw new IllegalArgumentException("Slip ID is required");
        }
        BorrowSlip borrowSlip = borrowDAO.findById(normalizedSlipId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow slip not found"));
        if (borrowSlip.getReturnDate() != null && !borrowSlip.getReturnDate().isBlank()) {
            throw new IllegalArgumentException("Borrow slip already returned");
        }
        if (returnDate == null) {
            throw new IllegalArgumentException("Return date is required");
        }
        LocalDate borrowDate = parseDateOrThrow(borrowSlip.getBorrowDate(), "Borrow date is invalid");
        if (returnDate.isBefore(borrowDate)) {
            throw new IllegalArgumentException("Return date cannot be before borrow date");
        }

        List<String> normalizedIsbnList = normalizeIsbnList(borrowSlip.getIsbnList());
        Map<String, Integer> borrowedCounts = countIsbns(normalizedIsbnList);
        List<String> normalizedLostIsbnList = normalizeIsbnList(lostIsbnList);
        Map<String, Integer> lostCounts = countIsbns(normalizedLostIsbnList);

        for (String isbn : borrowedCounts.keySet()) {
            bookService.getBookDAO().findByIsbn(isbn)
                    .orElseThrow(() -> new IllegalArgumentException("Book not found: " + isbn));
        }

        for (Map.Entry<String, Integer> lostEntry : lostCounts.entrySet()) {
            String isbn = lostEntry.getKey();
            int lostCount = lostEntry.getValue();
            int borrowedCount = borrowedCounts.getOrDefault(isbn, 0);
            if (borrowedCount == 0) {
                throw new IllegalArgumentException("Lost ISBN is not in this borrow slip: " + isbn);
            }
            if (lostCount > borrowedCount) {
                throw new IllegalArgumentException("Lost quantity exceeds borrowed quantity for ISBN: " + isbn);
            }
        }

        for (Map.Entry<String, Integer> entry : borrowedCounts.entrySet()) {
            int lostCount = lostCounts.getOrDefault(entry.getKey(), 0);
            int returnedCount = entry.getValue() - lostCount;
            if (returnedCount <= 0) {
                continue;
            }
            Book book = bookService.getBookDAO().findByIsbn(entry.getKey()).orElseThrow();
            book.setAvailableCopies(book.getAvailableCopies() + returnedCount);
        }
        bookService.saveBooks();

        borrowSlip.setReturnDate(returnDate.toString());
        borrowSlip.setLostIsbnList(new ArrayList<>(normalizedLostIsbnList));
        borrowDAO.saveBorrowSlips();
        return borrowSlip;
    }

    public PenaltyBreakdown calculatePenalty(BorrowSlip borrowSlip) {
        if (borrowSlip == null) {
            return new PenaltyBreakdown(0, 0L, 0L, 0L);
        }
        int overdueDays = calculateOverdueDays(borrowSlip);
        long overdueFine = overdueDays * OVERDUE_FINE_PER_DAY;
        long lostBookFine = calculateLostBookFine(borrowSlip.getLostIsbnList());
        return new PenaltyBreakdown(overdueDays, overdueFine, lostBookFine, overdueFine + lostBookFine);
    }

    public Optional<BorrowSlip> findBorrowSlipById(String slipId) {
        if (slipId == null || slipId.isBlank()) {
            return Optional.empty();
        }
        return borrowDAO.findById(slipId.trim());
    }

    public BorrowDAO getBorrowDAO() {
        return borrowDAO;
    }

    public List<BorrowSlip> getAllBorrowSlips() {
        return borrowDAO.getBorrowSlips();
    }

    public List<BorrowSlip> getActiveBorrowSlips() {
        return borrowDAO.getBorrowSlips().stream()
                .filter(slip -> slip.getReturnDate() == null || slip.getReturnDate().isBlank())
                .collect(Collectors.toList());
    }

    private List<String> normalizeIsbnList(List<String> isbnList) {
        if (isbnList == null) {
            return List.of();
        }
        return isbnList.stream()
                .map(value -> value == null ? "" : value.trim())
                .filter(value -> !value.isBlank())
                .collect(Collectors.toList());
    }

    private Map<String, Integer> countIsbns(List<String> isbnList) {
        Map<String, Integer> counts = new HashMap<>();
        for (String isbn : isbnList) {
            counts.put(isbn, counts.getOrDefault(isbn, 0) + 1);
        }
        return counts;
    }

    private int calculateOverdueDays(BorrowSlip borrowSlip) {
        LocalDate dueDate = parseDateOrNull(borrowSlip.getDueDate());
        LocalDate actualReturnDate = parseDateOrNull(borrowSlip.getReturnDate());
        if (dueDate == null || actualReturnDate == null || !actualReturnDate.isAfter(dueDate)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(dueDate, actualReturnDate);
    }

    private long calculateLostBookFine(List<String> lostIsbns) {
        long total = 0L;
        for (String isbn : normalizeIsbnList(lostIsbns)) {
            Book book = bookService.getBookDAO().findByIsbn(isbn)
                    .orElseThrow(() -> new IllegalArgumentException("Book not found: " + isbn));
            total += (long) book.getPrice() * LOST_BOOK_FINE_MULTIPLIER;
        }
        return total;
    }

    private LocalDate parseDateOrNull(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalDate parseDateOrThrow(String value, String errorMessage) {
        LocalDate parsed = parseDateOrNull(value);
        if (parsed == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        return parsed;
    }

    // Utils functions
    public int countBorrowSlips() {
        return borrowDAO.countBorrowSlips();
    }

    public int countOverdueBorrowSlips() {
        return borrowDAO.countOverdueBorrowSlips();
    }
}

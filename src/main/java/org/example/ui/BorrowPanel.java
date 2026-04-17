package org.example.ui;

import org.example.model.Book;
import org.example.model.BorrowSlip;
import org.example.model.Reader;
import org.example.service.BookService;
import org.example.service.BorrowService;
import org.example.service.ReaderService;
import org.example.util.PdfExportUtil;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class BorrowPanel extends JPanel {
    private final ReaderService readerService;
    private final BookService bookService;
    private final BorrowService borrowService;
    private DefaultTableModel tableModel;
    private JTable borrowTable;

    public BorrowPanel() {
        this(new ReaderService(), new BookService(), new BorrowService());
        initialize();
    }

    public BorrowPanel(ReaderService readerService, BookService bookService, BorrowService borrowService) {
        this.readerService = readerService;
        this.bookService = bookService;
        this.borrowService = borrowService;

        initialize();
    }

    private void initialize() {
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Borrow Slip Management"));

        JButton createBorrowButton = new JButton("Create Borrow Slip");
        createBorrowButton.addActionListener(e -> showCreateBorrowSlipDialog());
        topPanel.add(createBorrowButton);

        JButton returnBorrowButton = new JButton("Return Slip");
        returnBorrowButton.addActionListener(e -> showReturnSlipDialog());
        topPanel.add(returnBorrowButton);

        JButton showActiveButton = new JButton("Show Active");
        showActiveButton.addActionListener(e -> loadBorrowSlipsToTable(borrowService.getActiveBorrowSlips()));
        topPanel.add(showActiveButton);

        JButton showAllButton = new JButton("Show All");
        showAllButton.addActionListener(e -> loadBorrowSlipsToTable(borrowService.getAllBorrowSlips()));
        topPanel.add(showAllButton);

        JButton exportBorrowButton = new JButton("Export Borrow PDF");
        exportBorrowButton.addActionListener(e -> exportBorrowSlipPdf());
        topPanel.add(exportBorrowButton);

        JButton exportReturnButton = new JButton("Export Return PDF");
        exportReturnButton.addActionListener(e -> exportReturnSlipPdf());
        topPanel.add(exportReturnButton);

        String[] columns = {"Slip ID", "Reader ID", "Reader Name", "Book Titles", "Borrow Date", "Due Date", "Return Date", "ISBN List"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        borrowTable = new JTable(tableModel);
        loadBorrowSlipsToTable(borrowService.getAllBorrowSlips());

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(borrowTable), BorderLayout.CENTER);
    }

    // Load BorrowSlips
    private void loadBorrowSlipsToTable(List<BorrowSlip> slips) {
        tableModel.setRowCount(0);
        for (BorrowSlip slip : slips) {
            String isbnList = slip.getIsbnList() == null ? "" : String.join(",", slip.getIsbnList());
            String readerName = resolveReaderName(slip.getReaderId());
            String bookTitles = resolveBookTitles(slip.getIsbnList());
            tableModel.addRow(new Object[] {
                    slip.getSlipId(),
                    slip.getReaderId(),
                    readerName,
                    bookTitles,
                    slip.getBorrowDate(),
                    slip.getDueDate(),
                    slip.getReturnDate(),
                    isbnList,
            });
        }
    }

    // Show dialog to create borrow slip
    private void showCreateBorrowSlipDialog() {
        JTextField slipIdField = new JTextField(15);
        JTextField readerIdField = new JTextField(15);
        readerIdField.setEditable(false);
        JTextField borrowDateField = new JTextField(LocalDate.now().toString(), 15);
        JTextField dueDateField = new JTextField(LocalDate.now().plusDays(BorrowService.MAX_BORROW_DAYS).toString(), 15);
        JTextField selectedIsbnsField = new JTextField(20);
        selectedIsbnsField.setEditable(false);

        // Show Readers
        DefaultTableModel readerModel = new DefaultTableModel(new String[] {"Reader ID", "Full Name", "ID Card"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable readerTable = new JTable(readerModel);
        readerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loadReadersToTable(readerModel, readerService.getReaders());

        // Show Books
        DefaultTableModel bookModel = new DefaultTableModel(new String[] {"ISBN", "Title", "Available"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable bookTable = new JTable(bookModel);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loadBooksToTable(bookModel, bookService.getBooks());

        DefaultTableModel selectedBookModel = new DefaultTableModel(new String[] {"ISBN", "Title"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable selectedBookTable = new JTable(selectedBookModel);
        selectedBookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Map<String, String> selectedBooks = new LinkedHashMap<>();

        readerTable.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) {
                return;
            }
            int row = readerTable.getSelectedRow();
            if (row >= 0) {
                readerIdField.setText(String.valueOf(readerModel.getValueAt(row, 0)));
            }
        });

        Runnable refreshSelectedBooks = () -> {
            selectedBookModel.setRowCount(0);
            for (Map.Entry<String, String> entry : selectedBooks.entrySet()) {
                selectedBookModel.addRow(new Object[] {entry.getKey(), entry.getValue()});
            }
            selectedIsbnsField.setText(String.join(",", selectedBooks.keySet()));
        };

        JButton addBookButton = new JButton("Add Book");
        addBookButton.addActionListener(e -> {
            int row = bookTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please choose a book to add.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String isbn = String.valueOf(bookModel.getValueAt(row, 0));
            String title = String.valueOf(bookModel.getValueAt(row, 1));
            int available = Integer.parseInt(String.valueOf(bookModel.getValueAt(row, 2)));
            if (available <= 0) {
                JOptionPane.showMessageDialog(this, "Selected book is currently unavailable.", "Unavailable", JOptionPane.WARNING_MESSAGE);
                return;
            }
            selectedBooks.putIfAbsent(isbn, title);
            refreshSelectedBooks.run();
        });

        JButton removeBookButton = new JButton("Remove Selected");
        removeBookButton.addActionListener(e -> {
            int row = selectedBookTable.getSelectedRow();
            if (row < 0) {
                return;
            }
            String isbn = String.valueOf(selectedBookModel.getValueAt(row, 0));
            selectedBooks.remove(isbn);
            refreshSelectedBooks.run();
        });

        JButton clearBooksButton = new JButton("Clear");
        clearBooksButton.addActionListener(e -> {
            selectedBooks.clear();
            refreshSelectedBooks.run();
        });

        // Main form layout
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 6, 6));
        formPanel.add(new JLabel("Slip ID (optional):"));
        formPanel.add(slipIdField);
        formPanel.add(new JLabel("Selected Reader ID:"));
        formPanel.add(readerIdField);
        formPanel.add(new JLabel("Borrow Date (yyyy-MM-dd):"));
        formPanel.add(borrowDateField);
        formPanel.add(new JLabel("Due Date (yyyy-MM-dd):"));
        formPanel.add(dueDateField);
        formPanel.add(new JLabel("Selected ISBNs:"));
        formPanel.add(selectedIsbnsField);

        // Reader search panel
        JTextField readerSearchField = new JTextField(12);
        JButton readerSearchButton = new JButton("Find by Name");
        readerSearchButton.addActionListener(e ->
                loadReadersToTable(readerModel, readerService.findReadersByName(readerSearchField.getText())));
        JButton readerShowAllButton = new JButton("Show All");
        readerShowAllButton.addActionListener(e ->
                loadReadersToTable(readerModel, readerService.getReaders()));
        JPanel readerSearchPanel = new JPanel();
        readerSearchPanel.add(new JLabel("Reader Name:"));
        readerSearchPanel.add(readerSearchField);
        readerSearchPanel.add(readerSearchButton);
        readerSearchPanel.add(readerShowAllButton);
        JPanel readerPanel = new JPanel(new BorderLayout());
        readerPanel.add(readerSearchPanel, BorderLayout.NORTH);
        readerPanel.add(new JScrollPane(readerTable), BorderLayout.CENTER);

        // Book search panel
        JTextField bookSearchField = new JTextField(12);
        JButton bookSearchButton = new JButton("Find by Name");
        bookSearchButton.addActionListener(e ->
                loadBooksToTable(bookModel, bookService.searchByTitle(bookSearchField.getText())));
        JButton bookShowAllButton = new JButton("Show All");
        bookShowAllButton.addActionListener(e ->
                loadBooksToTable(bookModel, bookService.getBookDAO().getBooks()));
        JPanel bookSearchPanel = new JPanel();
        bookSearchPanel.add(new JLabel("Book Name:"));
        bookSearchPanel.add(bookSearchField);
        bookSearchPanel.add(bookSearchButton);
        bookSearchPanel.add(bookShowAllButton);
        JPanel bookPanel = new JPanel(new BorderLayout());
        bookPanel.add(bookSearchPanel, BorderLayout.NORTH);
        bookPanel.add(new JScrollPane(bookTable), BorderLayout.CENTER);

        // Selected books panel
        JPanel selectedBooksButtonPanel = new JPanel();
        selectedBooksButtonPanel.add(addBookButton);
        selectedBooksButtonPanel.add(removeBookButton);
        selectedBooksButtonPanel.add(clearBooksButton);
        JPanel selectedBooksPanel = new JPanel(new BorderLayout());
        selectedBooksPanel.add(new JLabel("Books in this slip"), BorderLayout.NORTH);
        selectedBooksPanel.add(new JScrollPane(selectedBookTable), BorderLayout.CENTER);
        selectedBooksPanel.add(selectedBooksButtonPanel, BorderLayout.SOUTH);

        JPanel selectionPanel = new JPanel(new GridLayout(1, 3, 8, 8));
        selectionPanel.add(readerPanel);
        selectionPanel.add(bookPanel);
        selectionPanel.add(selectedBooksPanel);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(selectionPanel, BorderLayout.CENTER);

        // Show dialog and handle result
        int result = JOptionPane.showConfirmDialog(this, panel, "Create Borrow Slip", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String slipId = slipIdField.getText().trim();
        String readerId = readerIdField.getText().trim();
        String borrowDateText = borrowDateField.getText().trim();
        String dueDateText = dueDateField.getText().trim();
        List<String> isbnList = new ArrayList<>(selectedBooks.keySet());

        if (readerId.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please select a reader.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (isbnList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one book.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            LocalDate borrowDate = parseDateOrDefault(borrowDateText, LocalDate.now());
            LocalDate dueDate = parseDateOrDefault(dueDateText, borrowDate.plusDays(14));
            borrowService.createBorrowSlip(slipId, readerId, borrowDate, dueDate, isbnList);
            loadBorrowSlipsToTable(borrowService.getAllBorrowSlips());
            JOptionPane.showMessageDialog(this, "Borrow slip created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showReturnSlipDialog() {
        int selectedRow = borrowTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a borrow slip to return.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String slipId = String.valueOf(tableModel.getValueAt(selectedRow, 0));
        JTextField returnDateField = new JTextField(LocalDate.now().toString(), 15);
        JTextField lostIsbnField = new JTextField(25);
        JPanel panel = new JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.add(new JLabel("Return Date (yyyy-MM-dd):"));
        panel.add(returnDateField);
        panel.add(new JLabel("Lost ISBNs (comma/semicolon separated, optional):"));
        panel.add(lostIsbnField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Return Borrow Slip", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            LocalDate returnDate = parseDateOrDefault(returnDateField.getText().trim(), LocalDate.now());
            List<String> lostIsbns = parseIsbnInput(lostIsbnField.getText());
            BorrowSlip returnedSlip = borrowService.returnBorrowSlip(slipId, returnDate, lostIsbns);
            BorrowService.PenaltyBreakdown penalty = borrowService.calculatePenalty(returnedSlip);
            loadBorrowSlipsToTable(borrowService.getAllBorrowSlips());
            String message = "Borrow slip returned successfully!\n"
                    + "Overdue fine: " + formatMoney(penalty.overdueFine()) + "\n"
                    + "Lost-book fine: " + formatMoney(penalty.lostBookFine()) + "\n"
                    + "Total fine: " + formatMoney(penalty.totalFine());
            JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Export PDF methods
    private void exportBorrowSlipPdf() {
        Optional<BorrowSlip> selectedSlip = getSelectedBorrowSlip();
        if (selectedSlip.isEmpty()) {
            return;
        }
        BorrowSlip slip = selectedSlip.get();
        Path outputPath = choosePdfPath("Save Borrow Slip PDF", "borrow-" + safeFileToken(slip.getSlipId()) + ".pdf");
        if (outputPath == null) {
            return;
        }
        String readerName = resolveReaderName(slip.getReaderId());
        List<Book> books = collectBooksFromIsbns(slip.getIsbnList());
        try {
            PdfExportUtil.exportBorrowSlip(outputPath, slip, readerName, books);
            JOptionPane.showMessageDialog(this, "Borrow slip exported to: " + outputPath, "Export Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportReturnSlipPdf() {
        Optional<BorrowSlip> selectedSlip = getSelectedBorrowSlip();
        if (selectedSlip.isEmpty()) {
            return;
        }
        BorrowSlip slip = selectedSlip.get();
        if (slip.getReturnDate() == null || slip.getReturnDate().isBlank()) {
            JOptionPane.showMessageDialog(this, "Please return this slip before exporting return PDF.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Path outputPath = choosePdfPath("Save Return Slip PDF", "return-" + safeFileToken(slip.getSlipId()) + ".pdf");
        if (outputPath == null) {
            return;
        }
        String readerName = resolveReaderName(slip.getReaderId());
        List<Book> books = collectBooksFromIsbns(slip.getIsbnList());
        BorrowService.PenaltyBreakdown penalty = borrowService.calculatePenalty(slip);
        try {
            PdfExportUtil.exportReturnSlip(outputPath, slip, readerName, books, penalty);
            JOptionPane.showMessageDialog(this, "Return slip exported to: " + outputPath, "Export Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Optional<BorrowSlip> getSelectedBorrowSlip() {
        int selectedRow = borrowTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a borrow slip first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return Optional.empty();
        }
        String slipId = String.valueOf(tableModel.getValueAt(selectedRow, 0));
        return borrowService.findBorrowSlipById(slipId)
                .or(() -> {
                    JOptionPane.showMessageDialog(this, "Borrow slip not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return Optional.empty();
                });
    }

    // Some helper methods for PDF export and data handling
    private Path choosePdfPath(String dialogTitle, String defaultFileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(dialogTitle);
        fileChooser.setSelectedFile(new java.io.File(defaultFileName));
        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        java.io.File selectedFile = fileChooser.getSelectedFile();
        String name = selectedFile.getName().toLowerCase();
        if (!name.endsWith(".pdf")) {
            selectedFile = new java.io.File(selectedFile.getParentFile(), selectedFile.getName() + ".pdf");
        }
        return selectedFile.toPath();
    }

    private List<Book> collectBooksFromIsbns(List<String> isbns) {
        if (isbns == null) {
            return List.of();
        }
        return isbns.stream()
                .map(isbn -> bookService.getBookDAO().findByIsbn(isbn).orElse(null))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<String> parseIsbnInput(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Stream.of(value.split("[,;]"))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .collect(Collectors.toList());
    }

    private String formatMoney(long amount) {
        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        return formatter.format(amount) + " VND";
    }

    private String safeFileToken(String value) {
        if (value == null || value.isBlank()) {
            return "slip";
        }
        return value.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private LocalDate parseDateOrDefault(String value, LocalDate defaultDate) {
        if (value == null || value.isBlank()) {
            return defaultDate;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format (yyyy-MM-dd)");
        }
    }

    private void loadReadersToTable(DefaultTableModel readerModel, List<Reader> readers) {
        readerModel.setRowCount(0);
        for (Reader reader : readers) {
            readerModel.addRow(new Object[] {
                    reader.getReaderId(),
                    reader.getFullName(),
                    reader.getIDCardNumber()
            });
        }
    }

    private void loadBooksToTable(DefaultTableModel bookModel, List<Book> books) {
        bookModel.setRowCount(0);
        for (Book book : books) {
            bookModel.addRow(new Object[] {
                    book.getIsbn(),
                    book.getTitle(),
                    book.getAvailableCopies()
            });
        }
    }

    private String resolveReaderName(String readerId) {
        return readerService.findReaderById(readerId)
                .map(Reader::getFullName)
                .orElse("");
    }

    private String resolveBookTitles(List<String> isbns) {
        if (isbns == null || isbns.isEmpty()) {
            return "";
        }
        return isbns.stream()
                .map(isbn -> bookService.getBookDAO().findByIsbn(isbn).map(Book::getTitle).orElse(isbn))
                .collect(Collectors.joining(", "));
    }
}

package org.example.ui;

import org.example.model.Book;
import org.example.model.BorrowSlip;
import org.example.model.Reader;
import org.example.service.BookService;
import org.example.service.BorrowService;
import org.example.service.ReaderService;

import javax.swing.JButton;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class BorrowPanel extends JPanel {
    private final BorrowService borrowService = new BorrowService();
    private final ReaderService readerService = new ReaderService();
    private final BookService bookService = new BookService();
    private DefaultTableModel tableModel;
    private JTable borrowTable;

    public BorrowPanel() {
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

    private void showCreateBorrowSlipDialog() {
        JTextField slipIdField = new JTextField(15);
        JTextField readerIdField = new JTextField(15);
        readerIdField.setEditable(false);
        JTextField borrowDateField = new JTextField(LocalDate.now().toString(), 15);
        JTextField dueDateField = new JTextField(LocalDate.now().plusDays(14).toString(), 15);
        JTextField isbnListField = new JTextField(20);
        isbnListField.setEditable(false);

        DefaultTableModel readerModel = new DefaultTableModel(new String[] {"Reader ID", "Full Name", "ID Card"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable readerTable = new JTable(readerModel);
        readerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loadReadersToTable(readerModel, readerService.getReaderDAO().getReaders());

        DefaultTableModel bookModel = new DefaultTableModel(new String[] {"ISBN", "Title", "Available"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable bookTable = new JTable(bookModel);
        bookTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        loadBooksToTable(bookModel, bookService.getBookDAO().getBooks());

        readerTable.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) {
                return;
            }
            int row = readerTable.getSelectedRow();
            if (row >= 0) {
                readerIdField.setText(String.valueOf(readerModel.getValueAt(row, 0)));
            }
        });

        bookTable.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) {
                return;
            }
            int[] rows = bookTable.getSelectedRows();
            List<String> isbns = new ArrayList<>();
            for (int row : rows) {
                isbns.add(String.valueOf(bookModel.getValueAt(row, 0)));
            }
            isbnListField.setText(String.join(",", isbns));
        });

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
        formPanel.add(isbnListField);

        JTextField readerSearchField = new JTextField(12);
        JButton readerSearchButton = new JButton("Find by Name");
        readerSearchButton.addActionListener(e ->
                loadReadersToTable(readerModel, readerService.findReadersByName(readerSearchField.getText())));
        JButton readerShowAllButton = new JButton("Show All");
        readerShowAllButton.addActionListener(e ->
                loadReadersToTable(readerModel, readerService.getReaderDAO().getReaders()));
        JPanel readerSearchPanel = new JPanel();
        readerSearchPanel.add(new JLabel("Reader Name:"));
        readerSearchPanel.add(readerSearchField);
        readerSearchPanel.add(readerSearchButton);
        readerSearchPanel.add(readerShowAllButton);
        JPanel readerPanel = new JPanel(new BorderLayout());
        readerPanel.add(readerSearchPanel, BorderLayout.NORTH);
        readerPanel.add(new JScrollPane(readerTable), BorderLayout.CENTER);

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

        JPanel selectionPanel = new JPanel(new GridLayout(1, 2, 8, 8));
        selectionPanel.add(readerPanel);
        selectionPanel.add(bookPanel);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(selectionPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create Borrow Slip", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String slipId = slipIdField.getText().trim();
        String readerId = readerIdField.getText().trim();
        String borrowDateText = borrowDateField.getText().trim();
        String dueDateText = dueDateField.getText().trim();
        List<String> isbnList = new ArrayList<>();
        for (int row : bookTable.getSelectedRows()) {
            isbnList.add(String.valueOf(bookModel.getValueAt(row, 0)));
        }

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
        JPanel panel = new JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.add(new JLabel("Return Date (yyyy-MM-dd):"));
        panel.add(returnDateField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Return Borrow Slip", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            LocalDate returnDate = parseDateOrDefault(returnDateField.getText().trim(), LocalDate.now());
            borrowService.returnBorrowSlip(slipId, returnDate);
            loadBorrowSlipsToTable(borrowService.getAllBorrowSlips());
            JOptionPane.showMessageDialog(this, "Borrow slip returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
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
        return bookService.searchByIsbn(isbns.getFirst()).stream()
                .map(Book::getTitle)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}

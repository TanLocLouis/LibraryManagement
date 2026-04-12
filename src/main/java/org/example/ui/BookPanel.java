package org.example.ui;

import org.example.model.Book;
import org.example.service.BookService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;

import static java.lang.Integer.parseInt;


public class BookPanel extends JPanel {
    private final BookService bookService = new BookService();
    private DefaultTableModel tableModel;
    private JTable bookTable;

    public BookPanel() {
        initialize();
    }

    void initialize() {
        // Top panel with title and add/edit/delete buttons
        JPanel contentPanel = new JPanel();
        contentPanel.add(new JLabel("Book Management"));

        JButton addBookButton = new JButton("Add Book");
        addBookButton.addActionListener(e -> showAddBookDialog());
        contentPanel.add(addBookButton);

        JButton editBookButton = new JButton("Edit Book");
        contentPanel.add(editBookButton);

        JButton deleteBookButton = new JButton("Delete Book");
        deleteBookButton.addActionListener(e -> deleteSelectedBook());
        contentPanel.add(deleteBookButton);

        // Search panel
        JPanel searchPanel = new JPanel();
        JTextField isbnSearchField = new JTextField(12);
        JButton isbnSearchButton = new JButton("Find by ISBN");
        searchPanel.add(new JLabel("ISBN:"));
        searchPanel.add(isbnSearchField);
        searchPanel.add(isbnSearchButton);

        JTextField nameSearchField = new JTextField(12);
        JButton nameSearchButton = new JButton("Find by Name");
        searchPanel.add(new JLabel("Book Name:"));
        searchPanel.add(nameSearchField);
        searchPanel.add(nameSearchButton);

        JButton showAllButton = new JButton("Show All");
        searchPanel.add(showAllButton);

        // Table to show books
        String[] columns = {"ISBN", "Title", "Author", "Publisher", "Year", "Category", "Quantity"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookTable = new JTable(tableModel);
        loadBookToTable();
        JScrollPane scrollPane = new JScrollPane(bookTable);

        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.NORTH);
        add(searchPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
    }

    public void showAddBookDialog() {
        JTextField isbnField = new JTextField(15);
        JTextField titleField = new JTextField(15);
        JTextField authorField = new JTextField(15);
        JTextField categoryField = new JTextField(15);
        JTextField publisherField = new JTextField(15);
        JTextField publishYearField = new JTextField(15);
        JTextField priceField = new JTextField(15);
        JTextField availableCopiesField  = new JTextField(15);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("ISBN:"));
        panel.add(isbnField);
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Author:"));
        panel.add(authorField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Publisher:"));
        panel.add(publisherField);
        panel.add(new JLabel("Publish Year:"));
        panel.add(publishYearField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Available Copies:"));
        panel.add(availableCopiesField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Book", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String isbn = isbnField.getText().trim();
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();
            String publisher = publisherField.getText().trim();
            int publishYear = parseInt(publishYearField.getText().trim());
            int price = parseInt(priceField.getText().trim());
            int quantity = parseInt(availableCopiesField.getText().trim());

            Book newBook = new Book(isbn, title, author, category, publisher, publishYear, price, quantity);
            if (!bookService.validate(newBook)) {
                JOptionPane.showMessageDialog(this, "Invalid book data. Please check your input.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            bookService.addBook(newBook);
            bookService.saveBooks();
            JOptionPane.showMessageDialog(this, "Book added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void loadBookToTable() {
        tableModel.setRowCount(0);
        for (Book book : bookService.getBookDAO().getBooks()) {
            Object[] row = {
                    book.getIsbn(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getPublisher(),
                    book.getPublishYear(),
                    book.getCategory(),
                    book.getAvailableCopies()
            };
            tableModel.addRow(row);}
    }

    private void deleteSelectedBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String isbn = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this book?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = bookService.deleteBook(isbn);
            if (deleted) {
                loadBookToTable();
                JOptionPane.showMessageDialog(this, "Book deleted successfully!", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Book not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

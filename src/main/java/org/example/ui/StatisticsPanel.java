package org.example.ui;

import org.example.service.BookService;
import org.example.service.BorrowService;
import org.example.service.ReaderService;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridLayout;

public class StatisticsPanel extends JPanel {
    public StatisticsPanel() {
        this(new ReaderService(), new BookService(), new BorrowService());
    }

    public StatisticsPanel(ReaderService readerService, BookService bookService, BorrowService borrowService) {
        setLayout(new GridLayout(3, 1, 8, 8));
        add(new JLabel("Readers: " + readerService.getReaderDAO().getReaders().size()));
        add(new JLabel("Books: " + bookService.getBookDAO().getBooks().size()));
        add(new JLabel("Borrow slips: " + borrowService.getAllBorrowSlips().size()));
    }
}


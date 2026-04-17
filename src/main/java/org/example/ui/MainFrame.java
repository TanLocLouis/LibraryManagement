package org.example.ui;

import org.example.service.BookService;
import org.example.service.BorrowService;
import org.example.service.LibrarianService;
import org.example.service.ReaderService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class MainFrame extends JFrame {
    private final LibrarianService librarianService;
    private final ReaderService readerService;
    private final BookService bookService;
    private final BorrowService borrowService;


    public MainFrame() {
        this(new LibrarianService());
    }

    public MainFrame(LibrarianService librarianService) {
        super("Library Management");
        this.librarianService = librarianService;
        this.readerService = new ReaderService();
        this.bookService = new BookService();
        this.borrowService = new BorrowService(bookService, readerService);
        initialize();
    }

    private void initialize() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Readers", new ReaderPanel(readerService));
        tabbedPane.addTab("Books", new BookPanel(bookService));
        tabbedPane.addTab("Borrow", new BorrowPanel(readerService, bookService, borrowService));
        tabbedPane.addTab("Statistics", new StatisticsPanel());

        JPanel topPanel = new JPanel(new BorderLayout());
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> handleLogout());
        topPanel.add(logoutButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        setSize(1200, 750);
        setLocationRelativeTo(null);
    }

    private void handleLogout() {
        LoginFrame loginFrame = new LoginFrame(librarianService);
        loginFrame.setVisible(true);
        dispose();
    }
}


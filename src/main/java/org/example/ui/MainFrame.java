package org.example.ui;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

public class MainFrame extends JFrame {
    public MainFrame() {
        super("Library Management");
        initialize();
    }

    private void initialize() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Readers", new ReaderPanel());
        tabbedPane.addTab("Books", new BookPanel());
        tabbedPane.addTab("Borrow", new BorrowPanel());
        tabbedPane.addTab("Statistics", new StatisticsPanel());

        add(tabbedPane, BorderLayout.CENTER);
        setSize(900, 600);
        setLocationRelativeTo(null);
    }
}


package org.example.ui;

import org.example.model.Reader;
import org.example.service.StatisticService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class StatisticsPanel extends JPanel {
    private final StatisticService statisticService;
    private final JLabel totalBooksLabel = new JLabel();
    private final JLabel borrowedBooksLabel = new JLabel();
    private final JLabel totalReadersLabel = new JLabel();
    private final DefaultTableModel categoryModel;
    private final DefaultTableModel genderModel;
    private final DefaultTableModel overdueModel;

    public StatisticsPanel() {
        this(new StatisticService());
    }

    public StatisticsPanel(StatisticService statisticService) {
        this.statisticService = statisticService;
        setLayout(new BorderLayout(8, 8));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 8, 8));
        summaryPanel.add(totalBooksLabel);
        summaryPanel.add(borrowedBooksLabel);
        summaryPanel.add(totalReadersLabel);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refresh());
        headerPanel.add(summaryPanel, BorderLayout.CENTER);
        headerPanel.add(refreshButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        categoryModel = new DefaultTableModel(new String[] {"Category", "Books"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        genderModel = new DefaultTableModel(new String[] {"Gender", "Readers"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        overdueModel = new DefaultTableModel(new String[] {"Reader ID", "Full Name", "Slip ID", "Due Date", "Days Overdue"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JPanel tablesPanel = new JPanel(new GridLayout(3, 1, 8, 8));
        tablesPanel.add(wrapTable("Books by Category", new JTable(categoryModel)));
        tablesPanel.add(wrapTable("Readers by Gender", new JTable(genderModel)));
        tablesPanel.add(wrapTable("Overdue Readers", new JTable(overdueModel)));
        add(tablesPanel, BorderLayout.CENTER);

        refresh();
    }

    private JPanel wrapTable(String title, JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refresh() {
        int totalBooks = statisticService.countTotalBooksInLibrary();
        int borrowedBooks = statisticService.countBorrowedBooks();
        int totalReaders = statisticService.countReaders();

        totalBooksLabel.setText("Total books in library: " + totalBooks);
        borrowedBooksLabel.setText("Books currently borrowed: " + borrowedBooks);
        totalReadersLabel.setText("Total readers: " + totalReaders);

        fillCategoryTable(statisticService.countBooksByCategory());
        fillGenderTable(statisticService.countReadersByGender());
        fillOverdueTable(statisticService.getOverdueEntries());
    }

    private void fillCategoryTable(Map<String, Integer> counts) {
        categoryModel.setRowCount(0);
        counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .forEach(entry -> categoryModel.addRow(new Object[] {entry.getKey(), entry.getValue()}));
    }

    private void fillGenderTable(Map<String, Integer> counts) {
        genderModel.setRowCount(0);
        counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .forEach(entry -> genderModel.addRow(new Object[] {entry.getKey(), entry.getValue()}));
    }

    private void fillOverdueTable(List<StatisticService.OverdueEntry> entries) {
        overdueModel.setRowCount(0);
        entries.stream()
                .sorted(Comparator.comparingLong(StatisticService.OverdueEntry::getOverdueDays).reversed())
                .forEach(entry -> {
                    Reader reader = entry.getReader();
                    String readerId = reader == null ? "" : reader.getReaderId();
                    String fullName = reader == null ? "" : reader.getFullName();
                    overdueModel.addRow(new Object[] {
                            readerId,
                            fullName,
                            entry.getSlip().getSlipId(),
                            entry.getSlip().getDueDate(),
                            entry.getOverdueDays()
                    });
                });
    }
}


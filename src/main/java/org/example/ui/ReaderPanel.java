package org.example.ui;

import org.example.model.Reader;
import org.example.service.ReaderService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class ReaderPanel extends JPanel {
    private final ReaderService readerService = new ReaderService();
    private DefaultTableModel tableModel;
    private JTable readerTable;

    public ReaderPanel() {
        initialize();
    }

    void initialize() {
        // Top panel with title and add button
        JPanel contentPanel = new JPanel();

        // Add reader button
        JButton addReaderButton = new JButton("Add Reader");
        addReaderButton.addActionListener(e -> showAddReaderDialog());
        contentPanel.add(addReaderButton);

        // Table to show readers
        String[] columns = {"ID", "Full Name", "ID Card", "DOB", "Gender", "Email", "Address", "Create Date", "Expire Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        readerTable = new JTable(tableModel);
        loadReadersToTable();
        JScrollPane scrollPane = new JScrollPane(readerTable);

        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadReadersToTable() {
        tableModel.setRowCount(0);
        List<Reader> readers = readerService.getReaderDAO().getReaders();
        for (Reader r : readers) {
            tableModel.addRow(new Object[] {
                r.getReaderId(),
                r.getFullName(),
                r.getIDCardNumber(),
                r.getDateOfBirth(),
                r.getGender(),
                r.getEmail(),
                r.getAddress(),
                r.getCreateDate(),
                r.getExpireDate()
            });
        }
    }

    private void showAddReaderDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField fullNameField = new JTextField();
        JTextField idCardField = new JTextField();
        JTextField dobField = new JTextField();
        JTextField genderField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField addressField = new JTextField();
        // Membership createDate is set to today
        // Expire date is today + 48 months
        // Reader ID is generated as UUID
        panel.add(new JLabel("Full Name:")); panel.add(fullNameField);
        panel.add(new JLabel("ID Card Number:")); panel.add(idCardField);
        panel.add(new JLabel("Date of Birth (yyyy-MM-dd):")); panel.add(dobField);
        panel.add(new JLabel("Gender:")); panel.add(genderField);
        panel.add(new JLabel("Email:")); panel.add(emailField);
        panel.add(new JLabel("Address:")); panel.add(addressField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Reader", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String readerId = UUID.randomUUID().toString();
            String fullName = fullNameField.getText().trim();
            String idCard = idCardField.getText().trim();
            String dob = dobField.getText().trim();
            String gender = genderField.getText().trim();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();
            String createDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String expireDate = LocalDate.now().plusMonths(48).format(DateTimeFormatter.ISO_DATE);
            Reader reader = new Reader(readerId, fullName, idCard, dob, gender, email, address, createDate, expireDate);
            if (!readerService.validate(reader)) {
                JOptionPane.showMessageDialog(this, "Full Name, Email, and Reader ID are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            readerService.getReaderDAO().addReader(reader);
            readerService.getReaderDAO().saveReaders();
            loadReadersToTable();
            JOptionPane.showMessageDialog(this, "Reader added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

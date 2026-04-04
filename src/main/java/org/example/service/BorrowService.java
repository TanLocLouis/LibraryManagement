package org.example.service;

import org.example.dao.BorrowDAO;
import org.example.model.BorrowSlip;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class BorrowService {
    private final BorrowDAO borrowDAO;

    public BorrowService() {
        this(new BorrowDAO());
    }

    public BorrowService(BorrowDAO borrowDAO) {
        this.borrowDAO = borrowDAO;
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

    public BorrowDAO getBorrowDAO() {
        return borrowDAO;
    }

    public List<BorrowSlip> getAllBorrowSlips() {
        return borrowDAO.getBorrowSlips();
    }
}


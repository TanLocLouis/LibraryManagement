package org.example.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BorrowSlip {
    private String slipId;
    private String readerId;
    private String borrowDate;
    private String dueDate;
    private String returnDate;
    private List<String> isbnList;
    private List<String> lostIsbnList;

    public BorrowSlip() {
        this.isbnList = new ArrayList<>();
        this.lostIsbnList = new ArrayList<>();
    }

    public BorrowSlip(String slipId, String readerId, String borrowDate, String dueDate, String returnDate, List<String> isbnList) {
        this(slipId, readerId, borrowDate, dueDate, returnDate, isbnList, List.of());
    }

    public BorrowSlip(String slipId, String readerId, String borrowDate, String dueDate, String returnDate, List<String> isbnList, List<String> lostIsbnList) {
        this.slipId = slipId;
        this.readerId = readerId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.isbnList = isbnList == null ? new ArrayList<>() : new ArrayList<>(isbnList);
        this.lostIsbnList = lostIsbnList == null ? new ArrayList<>() : new ArrayList<>(lostIsbnList);
    }

    public String getSlipId() {
        return slipId;
    }

    public void setSlipId(String slipId) {
        this.slipId = slipId;
    }

    public String getReaderId() {
        return readerId;
    }

    public void setReaderId(String readerId) {
        this.readerId = readerId;
    }

    public String getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(String borrowDate) {
        this.borrowDate = borrowDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public List<String> getIsbnList() {
        return isbnList;
    }

    public void setIsbnList(List<String> isbnList) {
        this.isbnList = isbnList == null ? new ArrayList<>() : new ArrayList<>(isbnList);
    }

    public List<String> getLostIsbnList() {
        return lostIsbnList;
    }

    public void setLostIsbnList(List<String> lostIsbnList) {
        this.lostIsbnList = lostIsbnList == null ? new ArrayList<>() : new ArrayList<>(lostIsbnList);
    }
}


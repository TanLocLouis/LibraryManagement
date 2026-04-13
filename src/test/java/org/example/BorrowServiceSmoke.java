package org.example;

import org.example.service.BorrowService;

public class BorrowServiceSmoke {
    public static void main(String[] args) {
        BorrowService service = new BorrowService();
        int total = service.getAllBorrowSlips().size();
        int active = service.getActiveBorrowSlips().size();
        System.out.println("Borrow slips: " + total + " (active " + active + ")");
    }
}


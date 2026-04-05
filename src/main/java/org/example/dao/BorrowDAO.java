package org.example.dao;

import org.example.model.BorrowSlip;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BorrowDAO {
    private static final Path FILE_PATH = Paths.get("data", "borrows.txt");
    private final ArrayList<BorrowSlip> borrowSlips = new ArrayList<>();

    public BorrowDAO() {
        loadBorrowSlips();
    }

    // Load and save
    public void loadBorrowSlips() {
        borrowSlips.clear();
        if (!Files.exists(FILE_PATH)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(FILE_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length < 6) {
                    continue;
                }
                List<String> isbnList = parts[5].isBlank()
                        ? Collections.emptyList()
                        : Arrays.stream(parts[5].split(","))
                        .map(String::trim)
                        .filter(value -> !value.isEmpty())
                        .collect(Collectors.toList());
                borrowSlips.add(new BorrowSlip(
                        parts[0],
                        parts[1],
                        parts[2],
                        parts[3],
                        parts[4],
                        isbnList
                ));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load borrow slips", e);
        }
    }

    public void saveBorrowSlips() {
        try {
            Path parent = FILE_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(FILE_PATH)) {
                for (BorrowSlip borrowSlip : borrowSlips) {
                    writer.write(String.join("|",
                            safe(borrowSlip.getSlipId()),
                            safe(borrowSlip.getReaderId()),
                            safe(borrowSlip.getBorrowDate()),
                            safe(borrowSlip.getDueDate()),
                            safe(borrowSlip.getReturnDate()),
                            String.join(",", borrowSlip.getIsbnList() == null ? List.of() : borrowSlip.getIsbnList())));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save borrow slips", e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    // Getter and Setter
    public ArrayList<BorrowSlip> getBorrowSlips() {
        return borrowSlips;
    }

    public Optional<BorrowSlip> findById(String slipId) {
        return borrowSlips.stream()
                .filter(slip -> slip.getSlipId() != null && slip.getSlipId().equals(slipId))
                .findFirst();
    }

    public void addBorrowSlip(BorrowSlip borrowSlip) {
        borrowSlips.add(borrowSlip);
    }

}


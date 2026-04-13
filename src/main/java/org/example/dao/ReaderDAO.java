package org.example.dao;

import org.example.model.Reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReaderDAO {
    private static final Path FILE_PATH = Paths.get("data", "readers.txt");
    private final ArrayList<Reader> readers = new ArrayList<>();

    public ReaderDAO() {
        loadReaders();
    }

    // Load and Save
    public void loadReaders() {
        readers.clear();
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
                if (parts.length < 9) {
                    continue;
                }
                readers.add(new Reader(
                        parts[0],
                        parts[1],
                        parts[2],
                        parts[3],
                        parts[4],
                        parts[5],
                        parts[6],
                        parts[7],
                        parts[8]
                ));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load readers", e);
        }
    }

    public void saveReaders() {
        try {
            Path parent = FILE_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(FILE_PATH)) {
                for (Reader reader : readers) {
                    writer.write(String.join("|",
                            safe(reader.getReaderId()),
                            safe(reader.getFullName()),
                            safe(reader.getDateOfBirth()),
                            safe(reader.getIDCardNumber()),
                            safe(reader.getGender()),
                            safe(reader.getEmail()),
                            safe(reader.getAddress()),
                            safe(reader.getCreateDate()),
                            safe(reader.getExpireDate())));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save readers", e);
        }
    }

    // Getters and Setters
    public ArrayList<Reader> getReaders() {
        return readers;
    }
    public void addReader(Reader reader) {
        readers.add(reader);
    }

    public void deleteReader(String readerId) {
        readers.removeIf(reader -> reader.getReaderId() != null && reader.getReaderId().equals(readerId));
        saveReaders();
    }

    public boolean updateReader(String readerId, Reader updatedReader) {
        for (int i = 0; i < readers.size(); i++) {
            if (readerId.equals(readers.get(i).getReaderId())) {
                readers.set(i, updatedReader);
                return true;
            }
        }
        return false;
    }

    // Utils
    public Optional<Reader> findById(String readerId) {
        return readers.stream()
                .filter(reader -> reader.getReaderId() != null && reader.getReaderId().equals(readerId))
                .findFirst();
    }

    public List<Reader> findByName(String name) {
        List<Reader> result = new ArrayList<>();
        if (name == null) return result;
        String lower = name.trim().toLowerCase();
        for (Reader r : readers) {
            if (r.getFullName() != null && r.getFullName().toLowerCase().contains(lower)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Reader> findByIDCardNumber(String idCardNumber) {
        List<Reader> result = new ArrayList<>();
        if (idCardNumber == null) return result;
        String trimmed = idCardNumber.trim();
        for (Reader r : readers) {
            if (r.getIDCardNumber() != null && r.getIDCardNumber().equals(trimmed)) {
                result.add(r);
            }
        }
        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public int countReaders() {
        return readers.size();
    }

    public int countReadersByGender(String gender) {
        if (gender == null) return 0;
        String lower = gender.trim().toLowerCase();
        int count = 0;
        for (Reader r : readers) {
            if (r.getGender() != null && r.getGender().trim().toLowerCase().equals(lower)) {
                count++;
            }
        }

        return count;
    }
}

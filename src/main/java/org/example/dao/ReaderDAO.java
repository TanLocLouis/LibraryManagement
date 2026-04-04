package org.example.dao;

import org.example.model.Reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

public class ReaderDAO {
    private static final Path FILE_PATH = Paths.get("data", "readers.txt");
    private final ArrayList<Reader> readers = new ArrayList<>();

    public ReaderDAO() {
        loadReaders();
    }

    public ArrayList<Reader> getReaders() {
        return readers;
    }

    public Optional<Reader> findById(String readerId) {
        return readers.stream()
                .filter(reader -> reader.getReaderId() != null && reader.getReaderId().equals(readerId))
                .findFirst();
    }

    public void addReader(Reader reader) {
        readers.add(reader);
    }

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
                        Boolean.parseBoolean(parts[8])
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
                            safe(reader.getGender()),
                            safe(reader.getAddress()),
                            safe(reader.getEmail()),
                            safe(reader.getPhoneNumber()),
                            safe(reader.getMembershipDate()),
                            Boolean.toString(reader.isActive())));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save readers", e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}


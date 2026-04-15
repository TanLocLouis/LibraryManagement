package org.example.dao;

import org.example.model.Librarian;
import org.example.util.CsvUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LibrarianDAO {
    private static final Path FILE_PATH = Paths.get("data", "accounts.csv");
    private static final Path LEGACY_FILE_PATH = Paths.get("data", "accounts.txt");
    private final ArrayList<Librarian> librarians = new ArrayList<>();

    public LibrarianDAO() {
        loadLibrarians();
    }

    public ArrayList<Librarian> getLibrarians() {
        return librarians;
    }

    public Optional<Librarian> findByUsername(String username) {
        return librarians.stream()
                .filter(librarian -> librarian.getUsername() != null && librarian.getUsername().equals(username))
                .findFirst();
    }

    // Load and Save
    public void loadLibrarians() {
        librarians.clear();
        if (Files.exists(FILE_PATH)) {
            loadFromCsv(FILE_PATH);
            return;
        }

        if (Files.exists(LEGACY_FILE_PATH)) {
            loadFromLegacyTxt(LEGACY_FILE_PATH);
            saveLibrarians();
        }
    }

    private void loadFromCsv(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> parts = CsvUtil.parseRow(line);
                if (parts.size() < 2) {
                    continue;
                }
                librarians.add(new Librarian(parts.get(0), parts.get(1)));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load librarians", e);
        }
    }

    private void loadFromLegacyTxt(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length < 2) {
                    continue;
                }
                librarians.add(new Librarian(parts[0], parts[1]));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load librarians", e);
        }
    }

    public void saveLibrarians() {
        try {
            Path parent = FILE_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(FILE_PATH)) {
                for (Librarian librarian : librarians) {
                    writer.write(CsvUtil.formatRow(List.of(
                            safe(librarian.getUsername()),
                            safe(librarian.getPassword()))));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save librarians", e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    // Getters and Setters
    public void addLibrarian(Librarian librarian) {
        librarians.add(librarian);
    }
}


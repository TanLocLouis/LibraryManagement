package org.example.service;

import org.example.dao.ReaderDAO;
import org.example.model.Reader;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReaderService {
    private final ReaderDAO readerDAO;

    public ReaderService() {
        this(new ReaderDAO());
    }

    public ReaderService(ReaderDAO readerDAO) {
        this.readerDAO = readerDAO;
    }

    public List<Reader> searchByName(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return readerDAO.getReaders().stream()
                .filter(reader -> reader.getFullName() != null
                        && reader.getFullName().toLowerCase(Locale.ROOT).contains(normalized))
                .collect(Collectors.toList());
    }

    public boolean validate(Reader reader) {
        return reader != null
                && notBlank(reader.getReaderId())
                && notBlank(reader.getFullName())
                && notBlank(reader.getPhoneNumber())
                && notBlank(reader.getEmail());
    }

    public ReaderDAO getReaderDAO() {
        return readerDAO;
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}


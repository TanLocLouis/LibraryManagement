package org.example.service;

import org.example.dao.LibrarianDAO;
import org.example.model.Librarian;

import java.util.Objects;

public class LibrarianService {
    private final LibrarianDAO librarianDAO;

    public LibrarianService() {
        this(new LibrarianDAO());
    }

    public LibrarianService(LibrarianDAO librarianDAO) {
        this.librarianDAO = librarianDAO;
    }

    public boolean authenticate(String username, String password) {
        return librarianDAO.findByUsername(username)
                .map(librarian -> Objects.equals(librarian.getPassword(), password))
                .orElse(false);
    }

    public boolean validate(Librarian librarian) {
        return librarian != null
                && librarian.getUsername() != null
                && !librarian.getUsername().isBlank();
    }

    public LibrarianDAO getLibrarianDAO() {
        return librarianDAO;
    }
}


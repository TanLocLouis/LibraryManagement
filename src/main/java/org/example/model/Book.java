package org.example.model;

import java.util.Objects;

public class Book {
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private int publishYear;
    private String category;
    private int price;
    private int availableCopies;

    public Book() {
    }

    public Book(String isbn, String title, String author, String category, String publisher, int publishYear, int price, int availableCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.publisher = publisher;
        this.publishYear = publishYear;
        this.price = price;
        this.availableCopies = availableCopies;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(int publishYear) {
        this.publishYear = publishYear;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTotalCopies() {
        return price;
    }

    public void setTotalCopies(int totalCopies) {
        this.price = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }
}


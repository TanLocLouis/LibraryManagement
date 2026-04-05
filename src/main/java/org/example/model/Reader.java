package org.example.model;

import java.util.Objects;

public class Reader {
    private String readerId;
    private String fullName;
    private String IDCardNumber;
    private String dateOfBirth;
    private String gender;
    private String email;
    private String address;
    private String createDate;
    private String expireDate;

    private final int EXPIRE_MONTHS = 48;

    //    Constructor
    public Reader() {
    }

    public Reader(String readerId, String fullName, String IDCardNumber, String dateOfBirth, String gender, String email, String address, String createDate, String expireDate) {
        this.readerId = readerId;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.IDCardNumber = IDCardNumber;
        this.gender = gender;
        this.email = email;
        this.address = address;
        this.createDate = createDate;
        this.expireDate = expireDate;
    }

    // Getters and Setters
    public String getReaderId() {
        return readerId;
    }

    public void setReaderId(String readerId) {
        this.readerId = readerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getIDCardNumber() {
        return IDCardNumber;
    }

    public void setIDCardNumber(String IDCardNumber) {
        this.IDCardNumber = IDCardNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }
}


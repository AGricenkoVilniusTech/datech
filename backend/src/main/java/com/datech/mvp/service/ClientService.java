package com.datech.mvp.service;

public class ClientService {

    public void validateClient(String name, String email) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (name.trim().length() < 2 || name.trim().length() > 100) {
            throw new IllegalArgumentException("Name must be between 2 and 100 characters");
        }

        if (email != null && !email.trim().isEmpty()) {
            if (!email.contains("@") || !email.contains(".")) {
                throw new IllegalArgumentException("Invalid email format");
            }
        }
    }
}
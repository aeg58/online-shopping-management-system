package com.osms.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public boolean matches(String enteredPassword, String storedPassword) {
        if (storedPassword.startsWith("$2")) {
            return encoder.matches(enteredPassword, storedPassword);
        }
        return storedPassword.equals(enteredPassword);
    }

    public String hash(String password) {
        return encoder.encode(password);
    }
}

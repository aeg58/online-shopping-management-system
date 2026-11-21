package com.osms.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Values starting with "$2" are BCrypt hashes. The sample data in
     * database/02_seed.sql uses placeholder values, which are compared as plain text.
     */
    public boolean matches(String enteredPassword, String storedPassword) {
        if (storedPassword.startsWith("$2")) {
            return encoder.matches(enteredPassword, storedPassword);
        }
        return storedPassword.equals(enteredPassword);
    }

    /** New accounts store only the BCrypt hash of the password, never the password itself. */
    public String hash(String password) {
        return encoder.encode(password);
    }
}

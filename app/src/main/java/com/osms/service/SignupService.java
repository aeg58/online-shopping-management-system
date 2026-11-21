package com.osms.service;

import com.osms.model.SessionUser;
import com.osms.model.SignupForm;
import com.osms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Creates new customer and seller accounts. Only these two roles can sign up; admins are added by hand.
 * A taken username, email, store name or tax number is rejected by the UNIQUE constraints of the database.
 */
@Service
public class SignupService {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_BYTES = 72; // BCrypt reads at most 72 bytes
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_.]+");

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public SignupService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    /** Inserts the Users row and the Customers row in one transaction. */
    @Transactional
    public void signUpCustomer(SignupForm form) {
        String firstName = text(form.getFirstName(), "First name", 50, true);
        String lastName = text(form.getLastName(), "Last name", 50, true);
        String phone = text(form.getPhone(), "Phone", 20, false);
        LocalDate birthDate = birthDate(form.getBirthDate());

        int userId = insertUser(SessionUser.ROLE_CUSTOMER, form);
        userRepository.insertCustomer(userId, firstName, lastName, phone, birthDate);
    }

    /** Inserts the Users row and the Sellers row in one transaction. The store waits for admin approval. */
    @Transactional
    public void signUpSeller(SignupForm form) {
        String storeName = text(form.getStoreName(), "Store name", 100, true);
        String taxNumber = text(form.getTaxNumber(), "Tax number", 20, true);
        String phone = text(form.getPhone(), "Phone", 20, false);

        int userId = insertUser(SessionUser.ROLE_SELLER, form);
        userRepository.insertSeller(userId, storeName, taxNumber, phone);
    }

    private int insertUser(String roleName, SignupForm form) {
        String username = text(form.getUsername(), "Username", 50, true);
        if (!USERNAME.matcher(username).matches()) {
            throw new SignupException("Username can only have letters, numbers, dots and underscores");
        }
        String email = text(form.getEmail(), "Email", 100, true);
        String password = form.getPassword() == null ? "" : form.getPassword();
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new SignupException("Password must have at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new SignupException("Password is too long");
        }
        if (!password.equals(form.getConfirmPassword())) {
            throw new SignupException("The passwords do not match");
        }

        int roleId = userRepository.findRoleId(roleName)
                .orElseThrow(() -> new SignupException("The " + roleName + " role is missing in the database"));
        return userRepository.insertUser(roleId, username, email, passwordService.hash(password));
    }

    private LocalDate birthDate(String value) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new SignupException("Birth date is not a valid date");
        }
    }

    private String text(String value, String label, int maxLength, boolean required) {
        String clean = value == null ? "" : value.trim();
        if (clean.isEmpty()) {
            if (required) {
                throw new SignupException(label + " is required");
            }
            return null;
        }
        if (clean.length() > maxLength) {
            throw new SignupException(label + " can have at most " + maxLength + " characters");
        }
        return clean;
    }
}

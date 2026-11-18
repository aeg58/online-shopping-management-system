package com.osms.service;

import com.osms.model.SessionUser;
import com.osms.model.UserAccount;
import com.osms.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public AuthService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    public SessionUser login(String username, String password) {
        UserAccount account = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new LoginFailedException("Invalid username or password"));

        if (!passwordService.matches(password, account.getPasswordHash())) {
            throw new LoginFailedException("Invalid username or password");
        }
        if (!account.isActive()) {
            throw new LoginFailedException("This account is disabled");
        }
        checkProfile(account);

        return new SessionUser(account.getUserId(), account.getUsername(), account.getRoleName(),
                account.getCustomerId(), account.getSellerId());
    }

    private void checkProfile(UserAccount account) {
        if (SessionUser.ROLE_SELLER.equals(account.getRoleName())) {
            if (account.getSellerId() == null) {
                throw new LoginFailedException("This account has no seller profile");
            }
            if (!Boolean.TRUE.equals(account.getSellerApproved())) {
                throw new LoginFailedException("Your store is waiting for admin approval");
            }
        }
        if (SessionUser.ROLE_CUSTOMER.equals(account.getRoleName()) && account.getCustomerId() == null) {
            throw new LoginFailedException("This account has no customer profile");
        }
    }
}

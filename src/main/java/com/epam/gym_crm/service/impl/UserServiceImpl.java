package com.epam.gym_crm.service.impl;

import com.epam.gym_crm.dto.request.ChangePasswordRequestDTO;
import com.epam.gym_crm.dto.request.LogInRequestDTO;
import com.epam.gym_crm.dto.response.UserResponseDTO;
import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.exception.InvalidPasswordException;
import com.epam.gym_crm.exception.InvalidUserCredentialException;
import com.epam.gym_crm.exception.UserNotFoundException;
import com.epam.gym_crm.mapper.UserMapper;
import com.epam.gym_crm.repository.UserRepository;
import com.epam.gym_crm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public User saveUser(User user) {
        log.info("Saving user: {}", user.getUsername());
        return userRepository.save(user);
    }

    @Override
    public User validateCredentials(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Username not found: {}", username);
                    return new InvalidUserCredentialException("Username or password is incorrect.");
                });

        if (!user.getPassword().equals(password)) {
            log.info("Password validation failed for user: {}", username);
            throw new InvalidUserCredentialException("Username or password is incorrect.");
        }

        return user;
    }

    @Transactional
    @Override
    public UserResponseDTO changePassword(ChangePasswordRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getUsername());
                    return new UserNotFoundException("User not found.");
                });

        if (!user.getPassword().equals(request.getOldPassword())) {
            log.error("Old password does not match for {}", request.getUsername());
            throw new InvalidPasswordException("Old password is incorrect.");
        }

        user.setPassword(request.getNewPassword());
        log.info("Password successfully changed for {}", request.getUsername());

        return userMapper.toUserResponseDTO(user);
    }

    @Override
    public String generateUsername(String firstName, String lastName) {

        log.info("Generating username for: {} {}", firstName, lastName);

        String baseUsername = firstName.trim().toLowerCase() + "." + lastName.trim().toLowerCase();
        String username = baseUsername;
        int suffix = 1;

        while (checkUsernameExists(username)) {
            log.warn("Username conflict: {} already exists. Trying next.", username);
            username = baseUsername + suffix;
            suffix++;
        }

        log.info("Generated unique username: {}", username);
        return username;
    }

    @Override
    public String generateRandomPassword() {
        String password = RANDOM.ints(10, 0, CHARACTERS.length())
                .mapToObj(CHARACTERS::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());

        log.info("Generated a random password.");
        return password;
    }

    @Transactional
    @Override
    public void updateStatus(String username) {
        try {
            int updatedCount = userRepository.toggleStatus(username);

            if (updatedCount > 0) {
                log.info("Successfully toggled status for user: {}", username);
            } else {
                log.warn("No user found with username: {}", username);
                throw new UserNotFoundException("User not found with username: " + username);
            }
        } catch (Exception e) {
            log.error("Error toggling status for user: {}", username, e);
            throw new ServiceException("Failed to toggle user status", e);
        }
    }

    @Override
    public void deleteUser(String username) {

        userRepository.findByUsername(username).ifPresentOrElse(user -> {
            userRepository.deleteByUsername(username);
            log.info("User deleted successfully: {}", username);
        }, () -> {
            log.warn("Attempted to delete non-existent user: {}", username);
            throw new RuntimeException("User not found.");
        });
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    @Override
    public UserResponseDTO login(LogInRequestDTO request) {

        User user = validateCredentials(request.getUsername(), request.getPassword());

        return userMapper.toUserResponseDTO(user);
    }

    private boolean checkUsernameExists(String username) {
        boolean exists = userRepository.findByUsername(username).isPresent();
        log.info("Checking if username exists ({}): {}", username, exists);
        return exists;
    }

}

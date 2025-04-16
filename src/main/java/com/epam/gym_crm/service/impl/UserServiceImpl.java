package com.epam.gym_crm.service.impl;

import com.epam.gym_crm.dto.request.ChangePasswordRequestDTO;
import com.epam.gym_crm.dto.request.LogInRequestDTO;
import com.epam.gym_crm.dto.response.AuthenticationResponseDTO;
import com.epam.gym_crm.dto.response.UserResponseDTO;
import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.exception.InvalidPasswordException;
import com.epam.gym_crm.exception.UserNotFoundException;
import com.epam.gym_crm.mapper.UserMapper;
import com.epam.gym_crm.repository.UserRepository;
import com.epam.gym_crm.service.JwtService;
import com.epam.gym_crm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public User saveUser(User user) {
        log.info("Saving user: {}", user.getUsername());
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public UserResponseDTO changePassword(ChangePasswordRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getUsername());
                    return new UserNotFoundException("User not found.");
                });

        // Here I am comparing with password encoder since BCrypt is used for hashing
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.error("Old password does not match for {}", request.getUsername());
            throw new InvalidPasswordException("Old password is incorrect.");
        }

        // Here we should encode the new password before saving
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
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
    public AuthenticationResponseDTO login(LogInRequestDTO request) {

        try { // Here the spring manages the username and password match check and returns a user, so I do not need to use repository
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername().toLowerCase(),
                            request.getPassword()
                    )
            ); // I will use this line of code quite a lot to take advantage of Spring Security Context Holder
            var user = (User) authentication.getPrincipal();
            var jwtAccessToken = jwtService.generateAccessToken(user);
            var jwtRefreshToken = jwtService.generateRefreshToken(user);

            return AuthenticationResponseDTO.builder()
                    .accessToken(jwtAccessToken)
                    .refreshToken(jwtRefreshToken)
                    .user(userMapper.toUserResponseDTO(user))
                    .build();
        } catch (Exception exception) {
            log.warn("User with these credentials does not exist");
            throw new UserNotFoundException("Wrong email and/or password");
        }
    }

    private boolean checkUsernameExists(String username) {
        boolean exists = userRepository.findByUsername(username).isPresent();
        log.info("Checking if username exists ({}): {}", username, exists);
        return exists;
    }

}

package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.request.ChangePasswordRequestDTO;
import com.epam.gym_crm.dto.response.UserResponseDTO;
import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.exception.InvalidPasswordException;
import com.epam.gym_crm.exception.InvalidUserCredentialException;
import com.epam.gym_crm.mapper.UserMapper;
import com.epam.gym_crm.repository.UserRepository;
import com.epam.gym_crm.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("john.doe");
        user.setPassword("password123");
        user.setIsActive(true);
    }

    @Test
    void testSaveUser() {
        when(userRepository.save(user)).thenReturn(user);

        User savedUser = userService.saveUser(user);

        assertNotNull(savedUser);
        assertEquals("john.doe", savedUser.getUsername());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testValidateCredentials() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        User user = userService.validateCredentials("john.doe", "password123");

        assertEquals("john.doe", user.getUsername());
        assertEquals("password123", user.getPassword());
        verify(userRepository, times(1)).findByUsername("john.doe");
    }

    @Test
    void testIsPasswordInvalid() {

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        assertThrows(InvalidUserCredentialException.class, () ->
                userService.validateCredentials("john.doe", "wrongpassword"));

        verify(userRepository, times(1)).findByUsername("john.doe");
    }


    @Test
    void testChangePassword() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        UserResponseDTO expectedResponse = new UserResponseDTO();
        expectedResponse.setUsername("john.doe");

        when(userMapper.toUserResponseDTO(user)).thenReturn(expectedResponse);

        UserResponseDTO userResponseDTO = userService.changePassword(new ChangePasswordRequestDTO("john.doe", "password123", "newpassword"));

        assertNotNull(userResponseDTO);
        assertEquals("john.doe", userResponseDTO.getUsername());

        assertEquals("newpassword", user.getPassword());

        verify(userRepository, times(1)).findByUsername("john.doe");
    }


    @Test
    void testChangePasswordWithIncorrectOldPassword() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        assertThrows(InvalidPasswordException.class, () -> userService.changePassword(new ChangePasswordRequestDTO("john.doe", "wrongpassword", "newpassword")));

        verify(userRepository, never()).updateUserPassword(anyString(), anyString());
    }

    @Test
    void testGenerateUsername() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.empty());

        String username = userService.generateUsername("John", "Doe");

        assertEquals("john.doe", username);
        verify(userRepository, times(1)).findByUsername("john.doe");
    }

    @Test
    void testGenerateUsernameWithConflict() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("john.doe1")).thenReturn(Optional.empty());

        String username = userService.generateUsername("John", "Doe");

        assertEquals("john.doe1", username);
        verify(userRepository, times(1)).findByUsername("john.doe");
        verify(userRepository, times(1)).findByUsername("john.doe1");
    }

    @Test
    void testGenerateRandomPassword() {
        String password = userService.generateRandomPassword();

        assertNotNull(password);
        assertEquals(10, password.length());
    }

    @Test
    void testUpdateStatus() {
        when(userRepository.toggleStatus("john.doe")).thenReturn(1);

        userService.updateStatus("john.doe");

        verify(userRepository, times(1)).toggleStatus("john.doe");
    }

    @Test
    void testDeleteUser() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        userService.deleteUser("john.doe");

        verify(userRepository, times(1)).deleteByUsername("john.doe");
    }

    @Test
    void testDeleteNonExistentUser() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.deleteUser("nonexistent"));

        verify(userRepository, never()).deleteByUsername(anyString());
    }

    @Test
    void testGetUserByUsername() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        User foundUser = userService.getUserByUsername("john.doe");

        assertNotNull(foundUser);
        assertEquals("john.doe", foundUser.getUsername());
        verify(userRepository, times(1)).findByUsername("john.doe");
    }

    @Test
    void testGetUserByUsernameNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserByUsername("nonexistent"));

        verify(userRepository, times(1)).findByUsername("nonexistent");
    }
}
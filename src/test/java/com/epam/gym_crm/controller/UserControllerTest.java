package com.epam.gym_crm.controller;

import com.epam.gym_crm.dto.request.ChangePasswordRequestDTO;
import com.epam.gym_crm.dto.request.LogInRequestDTO;
import com.epam.gym_crm.dto.response.UserResponseDTO;
import com.epam.gym_crm.exception.InvalidUserCredentialException;
import com.epam.gym_crm.handler.GlobalExceptionHandler;
import com.epam.gym_crm.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(globalExceptionHandler)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testLogin_Success() throws Exception {
        // Given
        LogInRequestDTO request = new LogInRequestDTO();
        request.setUsername("testUser");
        request.setPassword("testPass");

        UserResponseDTO response = new UserResponseDTO();
        response.setUsername("testUser");
        response.setIsActive(true);

        when(userService.login(any(LogInRequestDTO.class))).thenReturn(response);

        // Change from post() to get() and adjust parameters
        mockMvc.perform(get("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void testLogin_ValidationFailed_MissingUsername() throws Exception {
        // Given
        LogInRequestDTO request = new LogInRequestDTO();
        request.setPassword("testPass"); // username is missing

        // When & Then
        mockMvc.perform(get("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print()) // Add this to see detailed error
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.username").exists())
                .andExpect(jsonPath("$.errorMessage").value("One or more fields are invalid."));

        verify(userService, never()).login(any());
    }

    @Test
    void testChangePassword_Success() throws Exception {
        // Given
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setUsername("testUser");
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");

        UserResponseDTO response = new UserResponseDTO();
        response.setUsername("testUser");
        response.setIsActive(true);

        when(userService.changePassword(any(ChangePasswordRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/users/change-password")
                        .header("Username", "testUser")
                        .header("Password", "oldPass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(userService, times(1)).validateCredentials("testUser", "oldPass");
        verify(userService, times(1)).changePassword(any(ChangePasswordRequestDTO.class));
    }

    @Test
    void testChangePassword_MissingHeaders() throws Exception {
        // Given
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setUsername("testUser");
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");

        // When & Then
        mockMvc.perform(put("/api/v1/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()) // Changed from isBadRequest()
                .andExpect(jsonPath("$.errorMessage").value(containsString("Required header")));

        verify(userService, never()).validateCredentials(anyString(), anyString());
        verify(userService, never()).changePassword(any());
    }

    @Test
    void testChangePassword_ValidationFailed_MissingNewPassword() throws Exception {
        // Given
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setUsername("testUser");
        request.setOldPassword("oldPass");
        // newPassword is missing

        // When & Then
        mockMvc.perform(put("/api/v1/users/change-password")
                        .header("Username", "testUser")
                        .header("Password", "oldPass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).validateCredentials(anyString(), anyString());
        verify(userService, never()).changePassword(any());
    }

    @Test
    void testChangePassword_InvalidCredentials() throws Exception {
        // Given
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setUsername("testUser");
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");

        doThrow(new InvalidUserCredentialException("Username or password is incorrect."))
                .when(userService).validateCredentials("testUser", "wrongPass");

        // When & Then
        mockMvc.perform(put("/api/v1/users/change-password")
                        .header("Username", "testUser")
                        .header("Password", "wrongPass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.businessErrorCode").exists())
                .andExpect(jsonPath("$.errorMessage").value("Username or password is incorrect."));

        verify(userService, times(1)).validateCredentials("testUser", "wrongPass");
        verify(userService, never()).changePassword(any());
    }
}
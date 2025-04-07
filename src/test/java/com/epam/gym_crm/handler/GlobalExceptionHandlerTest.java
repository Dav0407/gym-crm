package com.epam.gym_crm.handler;

import com.epam.gym_crm.controller.UserController;
import com.epam.gym_crm.dto.request.ChangePasswordRequestDTO;
import com.epam.gym_crm.dto.request.LogInRequestDTO;
import com.epam.gym_crm.dto.response.ExceptionResponse;
import com.epam.gym_crm.exception.InvalidPasswordException;
import com.epam.gym_crm.exception.InvalidUserCredentialException;
import com.epam.gym_crm.exception.UserNotFoundException;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.epam.gym_crm.handler.BusinessErrorCodes.INTERNAL_ERROR;
import static com.epam.gym_crm.handler.BusinessErrorCodes.USER_NOT_FOUND;
import static com.epam.gym_crm.handler.BusinessErrorCodes.USER_UNAUTHORIZED;
import static com.epam.gym_crm.handler.BusinessErrorCodes.VALIDATION_FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(globalExceptionHandler)
                .build();
    }

    @Test
    void handleValidationExceptions_ForLoginWithMissingUsername_ShouldReturnValidationErrorResponse() throws Exception {
        // Given
        LogInRequestDTO loginRequest = LogInRequestDTO.builder()
                .password("password")
                .build();

        // When
        MvcResult result = mockMvc.perform(get("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        ExceptionResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(VALIDATION_FAILED.getCode(), response.getBusinessErrorCode());
        assertEquals(VALIDATION_FAILED.getDescription(), response.getBusinessErrorDescription());
        assertEquals("One or more fields are invalid.", response.getErrorMessage());
        assertNotNull(response.getValidationErrors());
        assertTrue(response.getValidationErrors().containsKey("username"));
        assertEquals("Username is required!", response.getValidationErrors().get("username"));
    }

    @Test
    void handleValidationExceptions_ForLoginWithMissingPassword_ShouldReturnValidationErrorResponse() throws Exception {
        // Given
        LogInRequestDTO loginRequest = LogInRequestDTO.builder()
                .username("testuser")
                .build();

        // When
        MvcResult result = mockMvc.perform(get("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        ExceptionResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(VALIDATION_FAILED.getCode(), response.getBusinessErrorCode());
        assertEquals(VALIDATION_FAILED.getDescription(), response.getBusinessErrorDescription());
        assertEquals("One or more fields are invalid.", response.getErrorMessage());
        assertNotNull(response.getValidationErrors());
        assertTrue(response.getValidationErrors().containsKey("password"));
        assertEquals("Password is required!", response.getValidationErrors().get("password"));
    }

    @Test
    void handleValidationExceptions_ForChangePasswordWithMissingFields_ShouldReturnValidationErrorResponse() throws Exception {
        // Given
        ChangePasswordRequestDTO changePasswordRequest = ChangePasswordRequestDTO.builder().build();

        // When
        MvcResult result = mockMvc.perform(put("/api/v1/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Then
        ExceptionResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(VALIDATION_FAILED.getCode(), response.getBusinessErrorCode());
        assertEquals(VALIDATION_FAILED.getDescription(), response.getBusinessErrorDescription());
        assertEquals("One or more fields are invalid.", response.getErrorMessage());
        assertNotNull(response.getValidationErrors());
        assertTrue(response.getValidationErrors().containsKey("username"));
        assertTrue(response.getValidationErrors().containsKey("oldPassword"));
        assertTrue(response.getValidationErrors().containsKey("newPassword"));
        assertEquals("Username is required", response.getValidationErrors().get("username"));
        assertEquals("Old password is required!", response.getValidationErrors().get("oldPassword"));
        assertEquals("New password is required!", response.getValidationErrors().get("newPassword"));
    }

    @Test
    void handleInvalidUserCredentialException_ShouldReturnUnauthorizedResponse() throws Exception {
        // Given
        LogInRequestDTO loginRequest = LogInRequestDTO.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        when(userService.login(any(LogInRequestDTO.class)))
                .thenThrow(new InvalidUserCredentialException("Invalid credentials"));

        // When
        MvcResult result = mockMvc.perform(get("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Then
        ExceptionResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(USER_UNAUTHORIZED.getCode(), response.getBusinessErrorCode());
        assertEquals(USER_UNAUTHORIZED.getDescription(), response.getBusinessErrorDescription());
        assertEquals("Invalid credentials", response.getErrorMessage());
    }

    @Test
    void handleInvalidPasswordException_ShouldReturnValidationFailedResponse() throws Exception {
        // Given
        ChangePasswordRequestDTO changePasswordRequest = ChangePasswordRequestDTO.builder()
                .username("testuser")
                .oldPassword("wrongpass")
                .newPassword("newpass")
                .build();

        // First mock the credential validation to succeed
        userService.validateCredentials("testuser", "validpass");

        // Then mock the password change to throw the exception
        when(userService.changePassword(any(ChangePasswordRequestDTO.class)))
                .thenThrow(new InvalidPasswordException("Old password is incorrect."));

        // When
        MvcResult result = mockMvc.perform(put("/api/v1/users/change-password")
                        .header("Username", "testuser")
                        .header("Password", "validpass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isBadRequest()) // Expect 400 for invalid password
                .andReturn();

        // Then
        ExceptionResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(VALIDATION_FAILED.getCode(), response.getBusinessErrorCode());
        assertEquals(VALIDATION_FAILED.getDescription(), response.getBusinessErrorDescription());
        assertEquals("Old password is incorrect.", response.getErrorMessage());
    }

    @Test
    void handleUserNotFoundException_ShouldReturnNotFoundResponse() throws Exception {
        // Given
        LogInRequestDTO loginRequest = LogInRequestDTO.builder()
                .username("nonexistentuser")
                .password("password")
                .build();

        when(userService.login(any(LogInRequestDTO.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        // When
        MvcResult result = mockMvc.perform(get("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNotFound())
                .andReturn();

        // Then
        ExceptionResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(USER_NOT_FOUND.getCode(), response.getBusinessErrorCode());
        assertEquals(USER_NOT_FOUND.getDescription(), response.getBusinessErrorDescription());
        assertEquals("User not found", response.getErrorMessage());
    }

    @Test
    void handleMissingRequestHeaderException_ShouldReturnUnauthorizedResponse() throws Exception {
        // Given
        ChangePasswordRequestDTO changePasswordRequest = ChangePasswordRequestDTO.builder()
                .username("testuser")
                .oldPassword("oldpass")
                .newPassword("newpass")
                .build();

        // When
        MvcResult result = mockMvc.perform(put("/api/v1/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Then
        ExceptionResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(USER_UNAUTHORIZED.getCode(), response.getBusinessErrorCode());
        assertEquals(USER_UNAUTHORIZED.getDescription(), response.getBusinessErrorDescription());

        // Check for the specific header missing message format
        assertTrue(response.getErrorMessage().matches("Required header '(Username|Password)' is missing"),
                "Error message should indicate which header is missing. Actual message: " + response.getErrorMessage());
    }

    @Test
    void handleException_ShouldReturnInternalServerErrorResponse() throws Exception {
        // Given
        LogInRequestDTO loginRequest = LogInRequestDTO.builder()
                .username("testuser")
                .password("password")
                .build();

        when(userService.login(any(LogInRequestDTO.class)))
                .thenThrow(new RuntimeException("Something went wrong"));

        // When
        MvcResult result = mockMvc.perform(get("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andReturn();

        // Then
        ExceptionResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), ExceptionResponse.class);

        assertEquals(INTERNAL_ERROR.getCode(), response.getBusinessErrorCode());
        assertEquals(INTERNAL_ERROR.getDescription(), response.getBusinessErrorDescription());
        assertEquals("Something went wrong", response.getErrorMessage());
    }
}
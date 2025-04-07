package com.epam.gym_crm.controller;

import com.epam.gym_crm.dto.request.ChangePasswordRequestDTO;
import com.epam.gym_crm.dto.request.LogInRequestDTO;
import com.epam.gym_crm.dto.response.UserResponseDTO;
import com.epam.gym_crm.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "User API", description = "API endpoints for user authentication and password management in the Gym CRM system")
public class UserController {

    private final UserService userService;

    @Operation(summary = "User login", description = "Authenticates a user and returns their profile details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid username or password", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> logIn(@Valid @RequestBody
                                                 @Parameter(description = "User login credentials (username and password)", required = true)
                                                 LogInRequestDTO request) {
        UserResponseDTO response = userService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Change user password", description = "Updates the password for an authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Password changed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials in headers", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping(value = "/change-password", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> changePassword(@Valid @RequestBody
                                                          @Parameter(description = "Details for changing the user's password", required = true)
                                                          ChangePasswordRequestDTO request,
                                                          @RequestHeader(value = "Username")
                                                          @Parameter(description = "Username for authentication", required = true, example = "admin")
                                                          String headerUsername,
                                                          @RequestHeader(value = "Password")
                                                          @Parameter(description = "Current password for authentication", required = true, example = "password123")
                                                          String headerPassword) {
        userService.validateCredentials(headerUsername, headerPassword);
        UserResponseDTO response = userService.changePassword(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
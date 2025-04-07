package com.epam.gym_crm.controller;

import com.epam.gym_crm.dto.request.ChangePasswordRequestDTO;
import com.epam.gym_crm.dto.request.LogInRequestDTO;
import com.epam.gym_crm.dto.response.UserResponseDTO;
import com.epam.gym_crm.service.UserService;
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

@Tag(name = "User API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> logIn(@Valid @RequestBody LogInRequestDTO request) {
        UserResponseDTO response = userService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(value = "/change-password", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponseDTO> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request,
                                                          @RequestHeader(value = "Username") String headerUsername,
                                                          @RequestHeader(value = "Password") String headerPassword) {
        userService.validateCredentials(headerUsername, headerPassword);
        UserResponseDTO response = userService.changePassword(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}

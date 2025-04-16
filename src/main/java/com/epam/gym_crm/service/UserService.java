package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.request.ChangePasswordRequestDTO;
import com.epam.gym_crm.dto.request.LogInRequestDTO;
import com.epam.gym_crm.dto.response.AuthenticationResponseDTO;
import com.epam.gym_crm.dto.response.UserResponseDTO;
import com.epam.gym_crm.entity.User;

public interface UserService {
    String generateUsername(String firstName, String lastName);

    String generateRandomPassword();

    User saveUser(User user);

    UserResponseDTO changePassword(ChangePasswordRequestDTO request);

    void updateStatus(String username);

    void deleteUser(String username);

    User getUserByUsername(String username);

    AuthenticationResponseDTO login(LogInRequestDTO request);
}

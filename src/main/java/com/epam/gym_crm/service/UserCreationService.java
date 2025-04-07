package com.epam.gym_crm.service;

import com.epam.gym_crm.entity.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

public interface UserCreationService {

    Log LOG = LogFactory.getLog(UserCreationService.class);

    UserService getUserService();  // To be implemented by services

    default User createUser(String firstName, String lastName) {
        if (!StringUtils.hasText(firstName) || !StringUtils.hasText(lastName)) {
            throw new IllegalArgumentException("First name and last name cannot be empty");
        }

        User user = User.builder()
                .firstName(firstName.trim())
                .lastName(lastName.trim())
                .username(getUserService().generateUsername(firstName, lastName))
                .password(getUserService().generateRandomPassword())
                .isActive(true)
                .build();

        user = getUserService().saveUser(user);
        LOG.info("User created successfully: " + user.toString());

        return user;
    }
}

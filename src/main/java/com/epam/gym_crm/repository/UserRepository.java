package com.epam.gym_crm.repository;

import com.epam.gym_crm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
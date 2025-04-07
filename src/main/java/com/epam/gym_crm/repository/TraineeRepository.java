package com.epam.gym_crm.repository;

import com.epam.gym_crm.entity.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {
    Optional<Trainee> findByUser_Id(Long userId);
    Optional<Trainee> findByUser_Username(String userUsername);
}
package com.epam.gym_crm.repository;

import com.epam.gym_crm.entity.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {
}
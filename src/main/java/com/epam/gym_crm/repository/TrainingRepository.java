package com.epam.gym_crm.repository;

import com.epam.gym_crm.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingRepository extends JpaRepository<Training, Long> {
}
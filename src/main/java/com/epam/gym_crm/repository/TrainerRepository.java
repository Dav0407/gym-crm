package com.epam.gym_crm.repository;

import com.epam.gym_crm.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
}
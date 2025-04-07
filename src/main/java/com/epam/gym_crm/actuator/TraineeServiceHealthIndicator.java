package com.epam.gym_crm.actuator;

import com.epam.gym_crm.repository.TraineeRepository;
import com.epam.gym_crm.service.TraineeService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TraineeServiceHealthIndicator implements HealthIndicator {

    private final TraineeService traineeService;
    private final TraineeRepository traineeRepository;

    public TraineeServiceHealthIndicator(TraineeService traineeService,
                                         TraineeRepository traineeRepository) {
        this.traineeService = traineeService;
        this.traineeRepository = traineeRepository;
    }

    @Override
    public Health health() {
        try {
            // Get basic statistics
            long traineeCount = traineeRepository.count();
            boolean isHealthy = traineeService.healthCheck();

            Health.Builder builder = isHealthy ? Health.up() : Health.down();

            return builder
                    .withDetail("message", isHealthy ?
                            "Trainee service is operational" :
                            "Trainee service health check failed")
                    .withDetail("trainee_count", traineeCount)
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("message", "Error checking trainee service health")
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}
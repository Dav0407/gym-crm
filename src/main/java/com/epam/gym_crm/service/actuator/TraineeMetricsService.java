package com.epam.gym_crm.service.actuator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class TraineeMetricsService {
    private final Counter traineeRegistrationCounter;
    private final Counter traineeProfileViewCounter;
    private final Counter traineeUpdateCounter;
    private final Counter traineeDeleteCounter;
    private final Counter traineeStatusChangeCounter;
    private final Timer traineeOperationTimer;

    public TraineeMetricsService(MeterRegistry registry) {
        traineeRegistrationCounter = Counter.builder("gym.trainee.registrations")
                .description("Total number of trainee registrations")
                .register(registry);

        traineeProfileViewCounter = Counter.builder("gym.trainee.profile.views")
                .description("Total number of trainee profile views")
                .register(registry);

        traineeUpdateCounter = Counter.builder("gym.trainee.updates")
                .description("Total number of trainee profile updates")
                .register(registry);

        traineeDeleteCounter = Counter.builder("gym.trainee.deletions")
                .description("Total number of trainee profile deletions")
                .register(registry);

        traineeStatusChangeCounter = Counter.builder("gym.trainee.status.changes")
                .description("Total number of trainee status changes")
                .register(registry);

        traineeOperationTimer = Timer.builder("gym.trainee.operation.time")
                .description("Time taken for trainee operations")
                .register(registry);
    }

    public void incrementRegistrationCount() {
        traineeRegistrationCounter.increment();
    }

    public void incrementProfileViewCount() {
        traineeProfileViewCounter.increment();
    }

    public void incrementUpdateCount() {
        traineeUpdateCounter.increment();
    }

    public void incrementDeleteCount() {
        traineeDeleteCounter.increment();
    }

    public void incrementStatusChangeCount() {
        traineeStatusChangeCounter.increment();
    }

    public Timer getOperationTimer() {
        return traineeOperationTimer;
    }
}
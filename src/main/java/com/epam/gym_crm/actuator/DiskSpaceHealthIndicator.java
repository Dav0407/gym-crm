package com.epam.gym_crm.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DiskSpaceHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        File root = new File("/");
        long freeSpace = root.getFreeSpace();
        if (freeSpace > 500 * 1024 * 1024) { // > 500MB
            return Health.up().withDetail("freeSpace", freeSpace).build();
        } else {
            return Health.down().withDetail("freeSpace", freeSpace).build();
        }
    }
}

package com.epam.gym_crm.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class ExternalApiHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        try {
            URL url = new URL("https://api.github.com");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000);
            conn.connect();
            if (conn.getResponseCode() == 200) {
                return Health.up().withDetail("github", "available").build();
            }
        } catch (Exception e) {
            return Health.down().withDetail("github", "unavailable").build();
        }
        return Health.unknown().build();
    }
}

package com.epam.gym_crm.config;

import com.epam.gym_crm.service.JwtService;
import feign.RequestInterceptor;
import org.slf4j.MDC;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Configuration
@RequiredArgsConstructor
public class FeignClientConfig {

    private final JwtService jwtService;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            // Set JWT Authorization Header
            SecurityContext securityContext = SecurityContextHolder.getContext();
            UserDetails userDetails = (UserDetails) securityContext.getAuthentication().getPrincipal();
            String accessToken = jwtService.generateAccessToken(userDetails);
            template.header("Authorization", "Bearer " + accessToken);

            // Set Transaction ID Header
            String transactionId = MDC.get("transactionId");
            if (transactionId != null) {
                template.header("X-Transaction-Id", transactionId);
            }
        };
    }

}

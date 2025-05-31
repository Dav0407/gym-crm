package com.epam.gym_crm.client;

import com.epam.gym_crm.service.JwtService;
import feign.RequestInterceptor;
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
            // Generate or retrieve JWT (access token)
           SecurityContext securityContext = SecurityContextHolder.getContext();
            UserDetails userDetails = (UserDetails) securityContext.getAuthentication().getPrincipal();
            String accessToken = jwtService.generateAccessToken(userDetails);
            template.header("Authorization", "Bearer " + accessToken);
        };
    }
}

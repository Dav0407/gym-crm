package com.epam.gym_crm.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class TransactionLoggingFilter implements Filter {

    private static final String TRANSACTION_ID = "transactionId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String transactionId = UUID.randomUUID().toString();
        MDC.put(TRANSACTION_ID, transactionId);

        try {
            System.out.printf("Transaction Start: [%s] %s %s%n", transactionId, httpRequest.getMethod(), httpRequest.getRequestURI());

            chain.doFilter(request, response);

            System.out.printf("Transaction End: [%s] Status: %d%n", transactionId, httpResponse.getStatus());
        } finally {
            MDC.remove(TRANSACTION_ID);
        }
    }
}

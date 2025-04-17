package com.epam.gym_crm.service;

import com.epam.gym_crm.service.impl.JwtServiceImpl;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    private JwtServiceImpl jwtService;

    @Mock
    private UserDetails userDetails;

    private static final String TEST_USERNAME = "testuser";
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long ACCESS_TOKEN_EXPIRATION = 3600000; // 1 hour
    private static final long REFRESH_TOKEN_EXPIRATION = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        jwtService = new JwtServiceImpl();

        // Set private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);

        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);
    }

    @Test
    void testGenerateAccessToken() {
        // Execute
        String token = jwtService.generateAccessToken(userDetails);

        // Verify
        assertNotNull(token);
        assertEquals(TEST_USERNAME, jwtService.extractUsername(token));
        assertFalse(jwtService.isRefreshToken(token));
    }

    @Test
    void testGenerateRefreshToken() {
        // Execute
        String token = jwtService.generateRefreshToken(userDetails);

        // Verify
        assertNotNull(token);
        assertEquals(TEST_USERNAME, jwtService.extractUsername(token));
        assertTrue(jwtService.isRefreshToken(token));
    }

    @Test
    void testExtractUsername() {
        // Setup
        String token = jwtService.generateAccessToken(userDetails);

        // Execute
        String username = jwtService.extractUsername(token);

        // Verify
        assertEquals(TEST_USERNAME, username);
    }

    @Test
    void testIsAccessTokenValid_ValidToken() {
        // Setup
        String token = jwtService.generateAccessToken(userDetails);

        // Execute & Verify
        assertTrue(jwtService.isAccessTokenValid(token, userDetails));
    }

    @Test
    void testIsAccessTokenValid_InvalidUsername() {
        // Setup
        String token = jwtService.generateAccessToken(userDetails);
        UserDetails differentUser = mock(UserDetails.class);
        when(differentUser.getUsername()).thenReturn("differentuser");

        // Execute & Verify
        assertFalse(jwtService.isAccessTokenValid(token, differentUser));
    }

    @Test
    void testIsAccessTokenValid_RefreshToken() {
        // Setup - using refresh token for access validation
        String token = jwtService.generateRefreshToken(userDetails);

        // Execute & Verify
        assertFalse(jwtService.isAccessTokenValid(token, userDetails));
    }

    @Test
    void testIsRefreshTokenValid_ValidToken() {
        // Setup
        String token = jwtService.generateRefreshToken(userDetails);

        // Execute & Verify
        assertTrue(jwtService.isRefreshTokenValid(token, userDetails));
    }

    @Test
    void testIsRefreshTokenValid_InvalidUsername() {
        // Setup
        String token = jwtService.generateRefreshToken(userDetails);
        UserDetails differentUser = mock(UserDetails.class);
        when(differentUser.getUsername()).thenReturn("differentuser");

        // Execute & Verify
        assertFalse(jwtService.isRefreshTokenValid(token, differentUser));
    }

    @Test
    void testIsRefreshTokenValid_AccessToken() {
        // Setup - using access token for refresh validation
        String token = jwtService.generateAccessToken(userDetails);

        // Execute & Verify
        assertFalse(jwtService.isRefreshTokenValid(token, userDetails));
    }

    @Test
    void testBlackListToken() throws Exception {
        // Setup
        String token = jwtService.generateAccessToken(userDetails);

        // Get access to blacklistedTokens map
        Field blacklistedTokensField = JwtServiceImpl.class.getDeclaredField("blacklistedTokens");
        blacklistedTokensField.setAccessible(true);
        Map<String, Date> blacklistedTokens = (Map<String, Date>) blacklistedTokensField.get(jwtService);

        // Execute
        jwtService.blackListToken(token);

        // Verify token is blacklisted
        assertTrue(blacklistedTokens.containsKey(token));

        // Verify token is no longer valid
        assertFalse(jwtService.isAccessTokenValid(token, userDetails));
    }

    @Test
    void testExtractClaim() {
        // Setup
        String token = jwtService.generateAccessToken(userDetails);
        Function<Claims, String> claimsResolver = Claims::getSubject;

        // Execute
        String subject = jwtService.extractClaim(token, claimsResolver);

        // Verify
        assertEquals(TEST_USERNAME, subject);
    }

    @Test
    void testIsRefreshToken_PositiveCase() {
        // Setup
        String token = jwtService.generateRefreshToken(userDetails);

        // Execute & Verify
        assertTrue(jwtService.isRefreshToken(token));
    }

    @Test
    void testIsRefreshToken_NegativeCase() {
        // Setup
        String token = jwtService.generateAccessToken(userDetails);

        // Execute & Verify
        assertFalse(jwtService.isRefreshToken(token));
    }
}
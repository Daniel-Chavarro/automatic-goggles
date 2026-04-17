package org.java_avanzado.taller.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private static final String TEST_SECRET = "NDBFNjM1MjY2NTU2QTU4NkUzMjcyMzU3NTM4NzgzRjQxM0Y0NDI4NDcyQjRCNjI1MDY0NTM2NzU2NkI1OTcw";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", 3_600_000L);
    }

    @Nested
    class GivenTokenGeneration {

        @Test
        void given_validEmail_when_generateTokenAndExtractUsername_then_roundTripsUsername() {
            String email = "ana@example.com";

            String token = jwtService.generateToken(email);
            String extractedUsername = jwtService.extractUsername(token);

            assertEquals(email, extractedUsername);
        }
    }

    @Nested
    class GivenTokenValidation {

        @Test
        void given_matchingUserAndToken_when_isTokenValid_then_returnsTrue() {
            String email = "ana@example.com";
            String token = jwtService.generateToken(email);

            boolean isValid = jwtService.isTokenValid(token, email);

            assertTrue(isValid);
        }

        @Test
        void given_malformedToken_when_extractingOrValidating_then_throwsNullPointerException() {
            String malformedToken = "not-a-jwt";

            assertThrows(NullPointerException.class, () -> jwtService.extractUsername(malformedToken));
            assertThrows(NullPointerException.class, () -> jwtService.isTokenValid(malformedToken, "ana@example.com"));
        }
    }
}

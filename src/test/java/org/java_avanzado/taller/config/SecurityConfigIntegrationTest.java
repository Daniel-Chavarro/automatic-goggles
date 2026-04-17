package org.java_avanzado.taller.config;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.java_avanzado.taller.service.JwtService;
import org.java_avanzado.taller.service.ProductService;
import org.java_avanzado.taller.utils.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@Import(SecurityConfigIntegrationTest.TestSecurityConfig.class)
class SecurityConfigIntegrationTest {

    private static final String PRODUCTS_ENDPOINT = "/api/products";
    private static final String VALID_TOKEN = "valid-token";
    private static final String INVALID_TOKEN = "invalid-token";
    private static final String USER_EMAIL = "security-test@demo.local";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductMapper productMapper;

    @Test
    void given_noAuthentication_when_requestProtectedEndpoint_then_returns401() throws Exception {
        mockMvc.perform(get(PRODUCTS_ENDPOINT))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(jwtService, userRepository, productService, productMapper);
    }

    @Test
    void given_invalidToken_when_requestProtectedEndpoint_then_returns401() throws Exception {
        when(jwtService.extractUsername(INVALID_TOKEN)).thenThrow(new RuntimeException("Invalid JWT"));

        mockMvc.perform(get(PRODUCTS_ENDPOINT)
                        .header("Authorization", "Bearer " + INVALID_TOKEN))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userRepository, productService, productMapper);
    }

    @Test
    void given_validToken_when_requestProtectedEndpoint_then_returns200() throws Exception {
        when(jwtService.extractUsername(VALID_TOKEN)).thenReturn(USER_EMAIL);
        when(jwtService.isTokenValid(VALID_TOKEN, USER_EMAIL)).thenReturn(true);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());
        when(productService.getAllActiveProducts()).thenReturn(List.of());
        when(productMapper.fromProductListToSummaryList(List.of())).thenReturn(List.of());

        mockMvc.perform(get(PRODUCTS_ENDPOINT)
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean(name = "securityFilterChain")
        SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/auth/**").permitAll()
                            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                            .anyRequest().authenticated()
                    )
                    .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .exceptionHandling(ex -> ex
                            .authenticationEntryPoint((request, response, authException) ->
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                            .accessDeniedHandler((request, response, accessDeniedException) ->
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                    )
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

            return http.build();
        }
    }
}

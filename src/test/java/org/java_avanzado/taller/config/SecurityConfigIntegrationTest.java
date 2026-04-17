package org.java_avanzado.taller.config;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
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
                .andExpect(status().isForbidden());

        verifyNoInteractions(jwtService, userRepository, productService, productMapper);
    }

    @Test
    void given_invalidToken_when_requestProtectedEndpoint_then_returns401() throws Exception {
        when(jwtService.extractUsername(INVALID_TOKEN)).thenThrow(new RuntimeException("Invalid JWT"));

        mockMvc.perform(get(PRODUCTS_ENDPOINT)
                        .header("Authorization", "Bearer " + INVALID_TOKEN))
                .andExpect(status().isForbidden());

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
}

package org.java_avanzado.taller.config;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import org.java_avanzado.taller.controller.dto.request.create.CreateProductRequest;
import org.java_avanzado.taller.controller.dto.response.ProductResponse;
import org.java_avanzado.taller.domain.model.Product;
import org.java_avanzado.taller.domain.model.UserRole;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.java_avanzado.taller.service.JwtService;
import org.java_avanzado.taller.service.ProductService;
import org.java_avanzado.taller.utils.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest {

    private static final String PRODUCTS_ENDPOINT = "/api/products";
    private static final String VALID_TOKEN = "valid-token";
    private static final String INVALID_TOKEN = "invalid-token";
    private static final String USER_EMAIL = "security-test@demo.local";
    private static final String ADMIN_EMAIL = "admin-security-test@demo.local";
    private static final String LOGIN_ENDPOINT = "/api/auth/login";

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
    void given_invalidToken_when_requestProtectedEndpoint_then_doesNotValidateTokenAgainstUser() throws Exception {
        when(jwtService.extractUsername(INVALID_TOKEN)).thenThrow(new RuntimeException("Invalid JWT"));

        mockMvc.perform(get(PRODUCTS_ENDPOINT)
                        .header("Authorization", "Bearer " + INVALID_TOKEN))
                .andExpect(status().isUnauthorized());

        verify(jwtService, never()).isTokenValid(INVALID_TOKEN, USER_EMAIL);
    }

    @Test
    void given_invalidToken_when_requestProtectedEndpoint_then_doesNotInvokeDownstreamServices() throws Exception {
        when(jwtService.extractUsername(INVALID_TOKEN)).thenThrow(new RuntimeException("Invalid JWT"));

        mockMvc.perform(get(PRODUCTS_ENDPOINT)
                        .header("Authorization", "Bearer " + INVALID_TOKEN))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userRepository, productService, productMapper);
    }

    @Test
    void given_validTokenAndMissingUser_when_requestProtectedEndpoint_then_returnsOk() throws Exception {
        when(jwtService.extractUsername(VALID_TOKEN)).thenReturn(USER_EMAIL);
        when(jwtService.isTokenValid(VALID_TOKEN, USER_EMAIL)).thenReturn(true);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());
        when(productService.getAllActiveProducts()).thenReturn(List.of());
        when(productMapper.fromProductListToSummaryList(List.of())).thenReturn(List.of());

        mockMvc.perform(get(PRODUCTS_ENDPOINT)
                        .header("Authorization", "Bearer " + VALID_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void given_validClientToken_when_createProduct_then_returnsForbidden() throws Exception {
        when(jwtService.extractUsername(VALID_TOKEN)).thenReturn(USER_EMAIL);
        when(jwtService.isTokenValid(VALID_TOKEN, USER_EMAIL)).thenReturn(true);
        UserEntity clientUser = new UserEntity();
        ReflectionTestUtils.setField(clientUser, "role", UserRole.CLIENT);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(clientUser));

        mockMvc.perform(post(PRODUCTS_ENDPOINT)
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType("application/json")
                        .content("""
                                {
                                  \"name\": \"Coffee\",
                                  \"description\": \"Ground coffee\",
                                  \"price\": 12.50,
                                  \"quantity\": 20
                                }
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(productService, productMapper);
    }

    @Test
    void given_validAdminToken_when_createProduct_then_returnsOk() throws Exception {
        Product createdProduct = mock(Product.class);
        ProductResponse response = mock(ProductResponse.class);

        when(jwtService.extractUsername(VALID_TOKEN)).thenReturn(ADMIN_EMAIL);
        when(jwtService.isTokenValid(VALID_TOKEN, ADMIN_EMAIL)).thenReturn(true);
        UserEntity adminUser = new UserEntity();
        ReflectionTestUtils.setField(adminUser, "role", UserRole.ADMIN);
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(adminUser));
        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(createdProduct);
        when(productMapper.fromProductToResponse(createdProduct)).thenReturn(response);

        mockMvc.perform(post(PRODUCTS_ENDPOINT)
                        .header("Authorization", "Bearer " + VALID_TOKEN)
                        .contentType("application/json")
                        .content("""
                                {
                                  \"name\": \"Coffee\",
                                  \"description\": \"Ground coffee\",
                                  \"price\": 12.50,
                                  \"quantity\": 20
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void given_missingLoginCredentials_when_requestPermitAllLoginEndpoint_then_returnsBadRequest() throws Exception {
        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(jwtService, userRepository, productService, productMapper);
    }
}

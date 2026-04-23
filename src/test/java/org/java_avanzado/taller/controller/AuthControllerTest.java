package org.java_avanzado.taller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Assertions;
import org.java_avanzado.taller.config.JwtAuthenticationFilter;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.service.UserService;
import org.java_avanzado.taller.support.TestDataFactory;
import org.java_avanzado.taller.utils.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.ArgumentCaptor;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private UserMapper userMapper;

    @Test
    void given_validRegisterPayload_when_register_then_returnsTokenResponse() throws Exception {
        User mappedUser = TestDataFactory.user();
        when(userMapper.fromRegisterUserRequestToDomain(any())).thenReturn(mappedUser);
        when(userService.registerUser(any(User.class))).thenReturn("jwt-token");

        String requestBody = """
                {
                  "email": "ana@example.com",
                  "firstName": "Ana",
                  "lastName": "Test",
                  "password": "S3cretPass"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).registerUser(userCaptor.capture());
        Assertions.assertEquals("ana@example.com", ReflectionTestUtils.getField(userCaptor.getValue(), "email"));
        Assertions.assertEquals("StrongPass1", ReflectionTestUtils.getField(userCaptor.getValue(), "password"));
    }

    @Test
    void given_missingCredentialsPayload_when_login_then_returnsBadRequest() throws Exception {
        String requestBody = """
                {
                  "email": "ana@example.com"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

}

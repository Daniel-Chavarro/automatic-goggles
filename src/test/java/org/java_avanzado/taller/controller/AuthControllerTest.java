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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void given_validRegisterPayload_when_register_then_returnsTokenResponse() throws Exception {
        when(userService.registerUser(any(User.class))).thenReturn("jwt-token");

        String requestBody = """
                {
                  \"email\": \"ana@example.com\",
                  \"password\": \"s3cret\"
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
        Assertions.assertEquals("s3cret", ReflectionTestUtils.getField(userCaptor.getValue(), "password"));
    }

    @Test
    void given_missingCredentialsPayload_when_login_then_returnsBadRequest() throws Exception {
        String requestBody = """
                {
                  \"email\": \"ana@example.com\"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @TestConfiguration
    static class AuthControllerTestConfig {
        @Bean
        JsonMapperBuilderCustomizer userMixinCustomizer() {
            return builder -> {
                builder.addMixIn(User.class, UserMixin.class);
                builder.addMixIn(User.UserBuilder.class, UserBuilderMixin.class);
            };
        }
    }

    @JsonDeserialize(builder = User.UserBuilder.class)
    private interface UserMixin {
    }

    @JsonPOJOBuilder(withPrefix = "")
    private interface UserBuilderMixin {
    }
}

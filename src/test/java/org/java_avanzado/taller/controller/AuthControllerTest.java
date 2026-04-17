package org.java_avanzado.taller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.java_avanzado.taller.config.JwtAuthenticationFilter;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    void given_validRegisterPayload_when_register_then_returns200AndToken() throws Exception {
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
    }

    @Test
    void given_missingCredentialsPayload_when_login_then_returns400() throws Exception {
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
        AbstractHttpMessageConverter<User> userMessageConverter() {
            return new AbstractHttpMessageConverter<>(MediaType.APPLICATION_JSON) {
                @Override
                protected boolean supports(Class<?> clazz) {
                    return User.class.isAssignableFrom(clazz);
                }

                @Override
                protected User readInternal(Class<? extends User> clazz, HttpInputMessage inputMessage)
                        throws IOException, HttpMessageNotReadableException {
                    String body = new String(inputMessage.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    User user = mock(User.class);
                    when(user.getEmail()).thenReturn(extractJsonValue(body, "email"));
                    when(user.getPassword()).thenReturn(extractJsonValue(body, "password"));
                    return user;
                }

                @Override
                protected void writeInternal(User user, org.springframework.http.HttpOutputMessage outputMessage) {
                }

                private String extractJsonValue(String body, String field) {
                    Pattern pattern = Pattern.compile("\\\"" + field + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
                    Matcher matcher = pattern.matcher(body);
                    return matcher.find() ? matcher.group(1) : null;
                }
            };
        }
    }
}

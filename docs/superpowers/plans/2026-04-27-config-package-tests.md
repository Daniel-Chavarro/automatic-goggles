# Configuration Package Tests Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a comprehensive, fast, and robust test suite for the configuration package using a hybrid strategy.

**Architecture:** A hybrid testing strategy using fast isolated unit tests (`ApplicationContextRunner`/Mockito) for bean verification and full context integration tests (`@SpringBootTest`/`MockMvc`) for behavioral validation. 

**Tech Stack:** Java, Spring Boot Test, JUnit 5, Mockito, Spring Security Test, MockMvc.

---

### Task 1: Setup E2E Context Load Smoke Test

**Files:**
- Create: `src/test/java/org/java_avanzado/taller/config/ContextLoadsE2ETest.java`

- [ ] **Step 1: Write the Smoke Test**

```java
package org.java_avanzado.taller.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Ensures we use H2
class ContextLoadsE2ETest {

    @Test
    void contextLoads() {
        // This test simply asserts that the Spring context starts successfully
        // without any BeanCreationException or dependency conflicts across all configurations.
    }
}
```

- [ ] **Step 2: Run test to verify it passes**

Run: `./mvnw test -Dtest=ContextLoadsE2ETest`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/java_avanzado/taller/config/ContextLoadsE2ETest.java
git commit -m "test: add E2E context load smoke test"
```

### Task 2: AsyncConfiguration Tests

**Files:**
- Create: `src/test/java/org/java_avanzado/taller/config/AsyncConfigurationTest.java`

- [ ] **Step 1: Write the Isolated Unit Test with ApplicationContextRunner**

```java
package org.java_avanzado.taller.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AsyncConfiguration.class));

    @Test
    void taskExecutor_BeanIsCreatedWithCorrectProperties() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ThreadPoolTaskExecutor.class);
            assertThat(context).hasBean("taskExecutor");

            ThreadPoolTaskExecutor executor = context.getBean(ThreadPoolTaskExecutor.class);
            assertThat(executor.getThreadNamePrefix()).isEqualTo("Tasks-Async-");
            assertThat(executor.getCorePoolSize()).isEqualTo(Runtime.getRuntime().availableProcessors());
            assertThat(executor.getMaxPoolSize()).isEqualTo(Runtime.getRuntime().availableProcessors() * 2);
        });
    }
}
```

- [ ] **Step 2: Run test to verify it passes**

Run: `./mvnw test -Dtest=AsyncConfigurationTest`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/java_avanzado/taller/config/AsyncConfigurationTest.java
git commit -m "test: add AsyncConfiguration isolated unit tests"
```

### Task 3: OpenApiConfig Tests

**Files:**
- Create: `src/test/java/org/java_avanzado/taller/config/OpenApiConfigTest.java`

- [ ] **Step 1: Write the Isolated Unit Test with ApplicationContextRunner**

```java
package org.java_avanzado.taller.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenApiConfig.class));

    @Test
    void customOpenAPI_BeanIsCreatedWithCorrectInfo() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OpenAPI.class);
            OpenAPI openAPI = context.getBean(OpenAPI.class);
            
            assertThat(openAPI.getInfo()).isNotNull();
            assertThat(openAPI.getInfo().getTitle()).isEqualTo("API REST TALLER FINAL");
            assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        });
    }
}
```

- [ ] **Step 2: Run test to verify it passes**

Run: `./mvnw test -Dtest=OpenApiConfigTest`
Expected: PASS

- [ ] **Step 3: Write the Integration Test for the Endpoint**

Modify: `src/test/java/org/java_avanzado/taller/config/OpenApiConfigTest.java`
```java
// Add nested integration test class at the bottom of the file
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Nested;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

// ... inside OpenApiConfigTest class
    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    class OpenApiIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void openApiDocs_AreAvailableAndUnsecured() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.info.title").value("API REST TALLER FINAL"));
        }
    }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw test -Dtest=OpenApiConfigTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/test/java/org/java_avanzado/taller/config/OpenApiConfigTest.java
git commit -m "test: add OpenApiConfig unit and integration tests"
```

### Task 4: BootstrapDataRunner Tests

**Files:**
- Create: `src/test/java/org/java_avanzado/taller/config/BootstrapDataRunnerTest.java`

- [ ] **Step 1: Write the Isolated Unit Test with Mockito**

```java
package org.java_avanzado.taller.config;

import org.java_avanzado.taller.domain.model.Role;
import org.java_avanzado.taller.persistence.repository.RoleRepository;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BootstrapDataRunnerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private BootstrapDataRunner bootstrapDataRunner;

    @Test
    void run_WhenUsersNotExist_ShouldInitializeData() throws Exception {
        when(userRepository.count()).thenReturn(0L);
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        bootstrapDataRunner.run();

        verify(roleRepository).save(any(Role.class)); // At least one save call
        verify(userRepository).save(any()); // Save admin user
    }

    @Test
    void run_WhenUsersExist_ShouldNotInitializeData() throws Exception {
        when(userRepository.count()).thenReturn(1L);

        bootstrapDataRunner.run();

        verify(roleRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }
}
```

- [ ] **Step 2: Run test to verify it passes**

Run: `./mvnw test -Dtest=BootstrapDataRunnerTest`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/java_avanzado/taller/config/BootstrapDataRunnerTest.java
git commit -m "test: add BootstrapDataRunner isolated tests"
```

### Task 5: SecurityConfig Endpoint Access Tests

**Files:**
- Create: `src/test/java/org/java_avanzado/taller/config/SecurityConfigTest.java`

- [ ] **Step 1: Write the Integration Test for Authorization Rules using MockMvc**

```java
package org.java_avanzado.taller.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpoints_ShouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void productsPostEndpoint_ShouldBeAccessibleByAdmin() throws Exception {
        // We expect a 400 Bad Request or 415 Unsupported Media Type if request is missing body, 
        // but crucially NOT 403 Forbidden
        mockMvc.perform(post("/api/products"))
               .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void productsPostEndpoint_ShouldBeForbiddenForClient() throws Exception {
        mockMvc.perform(post("/api/products"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void productsGetEndpoint_ShouldBeAccessibleByClient() throws Exception {
        mockMvc.perform(get("/api/products"))
               .andExpect(status().isOk()); // Assuming it returns empty list 200 OK
    }

    @Test
    void securedEndpoints_ShouldBeUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/products"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void usersEndpoint_ShouldBeForbiddenForClient() throws Exception {
        mockMvc.perform(get("/api/users"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void usersEndpoint_ShouldBeAccessibleByAdmin() throws Exception {
        // Checking it bypasses 403 (could be 200 or 400 depending on params)
        mockMvc.perform(get("/api/users"))
               .andExpect(status().isOk());
    }
}
```

- [ ] **Step 2: Run test to verify it passes**

Run: `./mvnw test -Dtest=SecurityConfigTest`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/java_avanzado/taller/config/SecurityConfigTest.java
git commit -m "test: add SecurityConfig integration tests"
```

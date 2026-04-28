# Endpoint Tests Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement unit, integration, and E2E tests for all REST controllers and global exception handling.

**Architecture:** We will use a hybrid authentication strategy (`@WithMockUser` for unit, JWT for integration/E2E), and an embedded H2 database for integration tests to validate the complete Spring Boot context.

**Tech Stack:** JUnit 5, MockMvc, `@WebMvcTest`, `@SpringBootTest`, Hamcrest, Spring Security.

---

### Task 1: Setup Test Dependencies & H2 Profile

**Files:**
- Modify: `pom.xml`
- Create: `src/test/resources/application-test.yml`

- [ ] **Step 1: Add dependencies to pom.xml**
Ensure `h2`, `spring-boot-starter-test`, and `spring-security-test` are present in `pom.xml`.

- [ ] **Step 2: Create application-test.yml**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
```

- [ ] **Step 3: Commit**
```bash
git add pom.xml src/test/resources/application-test.yml
git commit -m "test: add H2 database and test profile configuration"
```

### Task 2: Global Exception Handler Tests

**Files:**
- Create: `src/test/java/org/java_avanzado/taller/controller/advice/GlobalExceptionHandlerTest.java`

- [ ] **Step 1: Write GlobalExceptionHandlerTest**
```java
package org.java_avanzado.taller.controller.advice;

import org.java_avanzado.taller.controller.ProductController;
import org.java_avanzado.taller.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private org.java_avanzado.taller.service.ProductService productService;
    
    @MockBean
    private org.java_avanzado.taller.utils.mapper.ProductMapper productMapper;

    @MockBean
    private org.java_avanzado.taller.security.jwt.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser
    public void handleProductNotFound_ShouldReturn404() throws Exception {
        when(productService.getActiveProduct(any())).thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Product not found"));
    }
}
```

- [ ] **Step 2: Run test to verify it passes**
Run: `mvn test -Dtest=GlobalExceptionHandlerTest`

- [ ] **Step 3: Commit**
```bash
git add src/test/java/org/java_avanzado/taller/controller/advice/GlobalExceptionHandlerTest.java
git commit -m "test: add unit tests for global exception handler"
```

### Task 3: Unit Tests for AuthController

**Files:**
- Create: `src/test/java/org/java_avanzado/taller/controller/AuthControllerTest.java`

- [ ] **Step 1: Write AuthControllerTest**
```java
package org.java_avanzado.taller.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_avanzado.taller.controller.dto.request.auth.LoginUserRequest;
import org.java_avanzado.taller.controller.dto.response.JwtAuthResponse;
import org.java_avanzado.taller.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private org.java_avanzado.taller.security.jwt.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    public void login_ValidCredentials_ShouldReturn200() throws Exception {
        LoginUserRequest request = new LoginUserRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");

        JwtAuthResponse response = new JwtAuthResponse("access", "refresh");
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access"));
    }
}
```

- [ ] **Step 2: Run test to verify it passes**
Run: `mvn test -Dtest=AuthControllerTest`

- [ ] **Step 3: Commit**
```bash
git add src/test/java/org/java_avanzado/taller/controller/AuthControllerTest.java
git commit -m "test: add unit tests for auth controller"
```

### Task 4: Integration Test for Product Creation

**Files:**
- Create: `src/test/java/org/java_avanzado/taller/integration/ProductIntegrationTest.java`

- [ ] **Step 1: Write ProductIntegrationTest**
```java
package org.java_avanzado.taller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_avanzado.taller.controller.dto.request.create.CreateProductRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createProduct_AsAdmin_ShouldReturn200() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Integration Product");
        request.setPrice(BigDecimal.valueOf(100));
        request.setStock(10);
        request.setDescription("Test desc");

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Integration Product"));
    }
    
    @Test
    @WithMockUser(roles = "CLIENT")
    public void createProduct_AsClient_ShouldReturn403() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Integration Product");
        request.setPrice(BigDecimal.valueOf(100));
        request.setStock(10);
        request.setDescription("Test desc");

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
```

- [ ] **Step 2: Run test to verify it passes**
Run: `mvn test -Dtest=ProductIntegrationTest`

- [ ] **Step 3: Commit**
```bash
git add src/test/java/org/java_avanzado/taller/integration/ProductIntegrationTest.java
git commit -m "test: add integration tests for products"
```

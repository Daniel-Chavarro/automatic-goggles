# REST API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the missing Controllers, Services, and Mappers to expose full CRUD APIs for Users, Products, Orders, and complete the JWT Security Authentication flow.

**Architecture:** Standard Spring Boot layered architecture (Controller -> Service -> Persistence) with MapStruct for mapping and JJWT for token management.

**Tech Stack:** Java 21, Spring Boot, Spring Security, JWT, MapStruct, PostgreSQL.

---

### Task 1: JWT & Security Beans Configurations

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/config/SecurityBeansConfig.java`
- Create: `src/main/java/org/java_avanzado/taller/config/JwtService.java`
- Create: `src/main/java/org/java_avanzado/taller/config/JwtAuthenticationFilter.java`
- Create: `src/main/java/org/java_avanzado/taller/config/SecurityConfig.java`

- [ ] **Step 1: Create SecurityBeansConfig for PasswordEncoder**

```java
package org.java_avanzado.taller.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityBeansConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 2: Create JwtService**

```java
package org.java_avanzado.taller.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    // Minimum 256-bit key for HMAC-SHA256
    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, String userEmail) {
        final String username = extractUsername(token);
        return (username.equals(userEmail)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

- [ ] **Step 3: Create JwtAuthenticationFilter**

```java
package org.java_avanzado.taller.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.service.JwtService;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList; // Empty authorities for now

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Note: In a full app you'd fetch the user from DB to get Roles
            if (jwtService.isTokenValid(jwt, userEmail)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userEmail,
                        null,
                        new ArrayList<>() // Authorities/Roles could be parsed here
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

- [ ] **Step 4: Compile and Commit**

Run: `./mvnw compile`
Expected: Build SUCCESS

```bash
git add src/main/java/org/java_avanzado/taller/config/
git commit -m "feat(security): add jwt service, filter, and password encoder beans"
```

### Task 2: Security Configuration

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/config/SecurityConfig.java`

- [ ] **Step 1: Create SecurityConfig**

```java
package org.java_avanzado.taller.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

- [ ] **Step 2: Compile and Commit**

Run: `./mvnw compile`
Expected: Build SUCCESS

```bash
git add src/main/java/org/java_avanzado/taller/config/SecurityConfig.java
git commit -m "feat(security): configure stateless security filter chain"
```

### Task 3: User Mapper & User Service (Auth & CRUD)

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/utils/mapper/UserMapper.java`
- Create: `src/main/java/org/java_avanzado/taller/service/UserService.java`

- [ ] **Step 1: Create UserMapper**

```java
package org.java_avanzado.taller.utils.mapper;

import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toDomain(UserEntity entity);
    UserEntity toEntity(User domain);
}
```

- [ ] **Step 2: Create UserService**

```java
package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.domain.model.UserRole;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.java_avanzado.taller.utils.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public String registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        UserEntity entity = userMapper.toEntity(user);
        entity.setPassword(passwordEncoder.encode(user.getPassword()));
        entity.setActive(true);
        if (entity.getRole() == null) {
            entity.setRole(UserRole.CLIENT);
        }
        userRepository.save(entity);
        return jwtService.generateToken(entity.getEmail());
    }

    public String authenticate(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        if (!user.isActive()) {
            throw new RuntimeException("Account disabled");
        }
        return jwtService.generateToken(email);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).map(userMapper::toDomain)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public void disableUser(UUID id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        entity.setActive(false);
        userRepository.save(entity);
    }
}
```

- [ ] **Step 3: Add findByEmail to UserRepository**

```java
// Inside src/main/java/org/java_avanzado/taller/persistence/repository/UserRepository.java
// Add the following method inside the interface:
    java.util.Optional<org.java_avanzado.taller.persistence.entity.UserEntity> findByEmail(String email);
```

- [ ] **Step 4: Compile and Commit**

Run: `./mvnw compile`
Expected: Build SUCCESS

```bash
git add src/main/java/org/java_avanzado/taller/utils/mapper/UserMapper.java
git add src/main/java/org/java_avanzado/taller/service/UserService.java
git add src/main/java/org/java_avanzado/taller/persistence/repository/UserRepository.java
git commit -m "feat(service): add UserMapper and UserService with auth logic"
```

### Task 4: AuthController and UserController

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/controller/AuthController.java`
- Create: `src/main/java/org/java_avanzado/taller/controller/UserController.java`

- [ ] **Step 1: Create AuthController**

```java
package org.java_avanzado.taller.controller;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        String token = userService.registerUser(user);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String token = userService.authenticate(credentials.get("email"), credentials.get("password"));
        return ResponseEntity.ok(Map.of("token", token));
    }
}
```

- [ ] **Step 2: Create UserController**

```java
package org.java_avanzado.taller.controller;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disableUser(@PathVariable UUID id) {
        userService.disableUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 3: Compile and Commit**

Run: `./mvnw compile`
Expected: Build SUCCESS

```bash
git add src/main/java/org/java_avanzado/taller/controller/
git commit -m "feat(controller): add AuthController and UserController endpoints"
```

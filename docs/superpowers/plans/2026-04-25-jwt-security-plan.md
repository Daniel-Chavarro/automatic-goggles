# JWT Security Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement full JWT-based stateless authentication using Spring Security with CustomUserDetails, Refresh Token rotation, and a JwtAuthenticationFilter.

**Architecture:** Use CustomUserDetails wrapping UserEntity, implement UserDetailsService, create AuthService orchestrator, add JwtAuthenticationFilter, and update SecurityConfig to enforce auth on all endpoints except /api/auth/** and swagger.

**Tech Stack:** Spring Security 6, jjwt, Spring Data JPA, Lombok

---

### File Structure

- Create: `src/main/java/org/java_avanzado/taller/security/CustomUserDetails.java`
- Create: `src/main/java/org/java_avanzado/taller/security/CustomUserDetailsService.java`
- Create: `src/main/java/org/java_avanzado/taller/controller/dto/response/JwtAuthResponse.java`
- Create: `src/main/java/org/java_avanzado/taller/service/AuthService.java`
- Create: `src/main/java/org/java_avanzado/taller/security/jwt/JwtAuthenticationFilter.java`
- Modify: `src/main/java/org/java_avanzado/taller/config/SecurityConfig.java`
- Modify: `src/main/java/org/java_avanzado/taller/service/JwtService.java`
- Modify: `src/main/java/org/java_avanzado/taller/service/RefreshTokenService.java`
- Modify: `src/main/java/org/java_avanzado/taller/controller/AuthController.java`

---

### Task 1: Create CustomUserDetails

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/security/CustomUserDetails.java`

- [ ] **Step 1: Write CustomUserDetails class**

```java
package org.java_avanzado.taller.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.domain.model.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Getter
public class CustomUserDetails implements UserDetails {

    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    public UUID getId() {
        return user.getId();
    }

    public UserRole getRole() {
        return user.getRole();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/security/CustomUserDetails.java
git commit -m "feat: add CustomUserDetails implementing UserDetails"
```

---

### Task 2: Create CustomUserDetailsService

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/security/CustomUserDetailsService.java`

- [ ] **Step 1: Write CustomUserDetailsService class**

```java
package org.java_avanzado.taller.security;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/security/CustomUserDetailsService.java
git commit -m "feat: add CustomUserDetailsService implementing UserDetailsService"
```

---

### Task 3: Create JwtAuthResponse DTO

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/controller/dto/response/JwtAuthResponse.java`

- [ ] **Step 1: Write JwtAuthResponse class**

```java
package org.java_avanzado.taller.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtAuthResponse {

    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/controller/dto/response/JwtAuthResponse.java
git commit -m "feat: add JwtAuthResponse DTO for auth endpoints"
```

---

### Task 4: Create AuthService

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/service/AuthService.java`

- [ ] **Step 1: Write AuthService class**

```java
package org.java_avanzado.taller.service;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.auth.LoginUserRequest;
import org.java_avanzado.taller.controller.dto.request.auth.RegisterUserRequest;
import org.java_avanzado.taller.controller.dto.response.JwtAuthResponse;
import org.java_avanzado.taller.exception.UserNotFoundException;
import org.java_avanzado.taller.persistence.entity.UserEntity;
import org.java_avanzado.taller.persistence.repository.UserRepository;
import org.java_avanzado.taller.security.CustomUserDetails;
import org.java_avanzado.taller.security.CustomUserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public JwtAuthResponse register(RegisterUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        UserEntity user = UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .active(true)
                .build();

        user = userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return JwtAuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public JwtAuthResponse login(LoginUserRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserEntity user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UserNotFoundException(userDetails.getId()));

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return JwtAuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public JwtAuthResponse refresh(String refreshToken) {
        refreshTokenService.validateRefreshToken(refreshToken);

        String email = refreshTokenService.getUserEmailFromRefreshToken(refreshToken);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);

        return JwtAuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void logout(String refreshToken) {
        refreshTokenService.deleteRefreshToken(refreshToken);
    }
}
```

- [ ] **Step 2: Check RefreshTokenService for required methods**

Read `src/main/java/org/java_avanzado/taller/service/RefreshTokenService.java` to verify it has:
- `createRefreshToken(UserEntity user)`
- `validateRefreshToken(String token)`
- `getUserEmailFromRefreshToken(String token)`
- `deleteRefreshToken(String token)`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/service/AuthService.java
git commit -m "feat: add AuthService for login, register, logout and refresh"
```

---

### Task 5: Update RefreshTokenService

**Files:**
- Modify: `src/main/java/org/java_avanzado/taller/service/RefreshTokenService.java`

- [ ] **Step 1: Read current RefreshTokenService**

```bash
read src/main/java/org/java_avanzado/taller/service/RefreshTokenService.java
```

- [ ] **Step 2: Add required methods if missing**

Add these methods if they don't exist:

```java
public String createRefreshToken(UserEntity user) {
    String token = UUID.randomUUID().toString();
    RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
            .token(token)
            .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
            .user(user)
            .build();
    refreshTokenRepository.save(refreshToken);
    return token;
}

public void validateRefreshToken(String token) {
    RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));
    if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0) {
        refreshTokenRepository.delete(refreshToken);
        throw new TokenRefreshExpiredException("Refresh token expired");
    }
}

public String getUserEmailFromRefreshToken(String token) {
    RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new TokenRefreshException("Refresh token not found"));
    return refreshToken.getUser().getEmail();
}

public void deleteRefreshToken(String token) {
    refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
}
```

Add field `@Value("${jwt.refresh-expiration:86400000}") private long refreshTokenExpiration;`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/service/RefreshTokenService.java
git commit -m "feat: add refresh token management methods to RefreshTokenService"
```

---

### Task 6: Create JwtAuthenticationFilter

**Files:**
- Create: `src/main/java/org/java_avanzado/taller/security/jwt/JwtAuthenticationFilter.java`

- [ ] **Step 1: Write JwtAuthenticationFilter class**

```java
package org.java_avanzado.taller.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.security.CustomUserDetails;
import org.java_avanzado.taller.security.CustomUserDetailsService;
import org.java_avanzado.taller.service.JwtService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        
        try {
            userEmail = jwtService.extractUsername(jwt);
            
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);
                
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/security/jwt/JwtAuthenticationFilter.java
git commit -m "feat: add JwtAuthenticationFilter for stateless auth"
```

---

### Task 7: Update SecurityConfig

**Files:**
- Modify: `src/main/java/org/java_avanzado/taller/config/SecurityConfig.java`

- [ ] **Step 1: Update SecurityConfig**

```java
package org.java_avanzado.taller.config;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.security.CustomUserDetailsService;
import org.java_avanzado.taller.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/config/SecurityConfig.java
git commit -m "feat: update SecurityConfig to enforce JWT authentication"
```

---

### Task 8: Update AuthController

**Files:**
- Modify: `src/main/java/org/java_avanzado/taller/controller/AuthController.java`

- [ ] **Step 1: Update AuthController to use AuthService**

```java
package org.java_avanzado.taller.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.controller.dto.request.auth.LoginUserRequest;
import org.java_avanzado.taller.controller.dto.request.auth.RegisterUserRequest;
import org.java_avanzado.taller.controller.dto.response.JwtAuthResponse;
import org.java_avanzado.taller.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<JwtAuthResponse> register(@RequestBody @Valid RegisterUserRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody @Valid LoginUserRequest credentials) {
        return ResponseEntity.ok(authService.login(credentials));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthResponse> refresh(@RequestBody String refreshToken) {
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }
}
```

Adjust imports as needed.

- [ ] **Step 2: Commit**

```bash
git add src/main/java/org/java_avanzado/taller/controller/AuthController.java
git commit -m "feat: update AuthController to use AuthService"
```

---

### Task 9: Verify and Test Build

**Files:**
- Run: `mvn compile`

- [ ] **Step 1: Run build to verify compilation**

Run: `mvn clean compile`
Expected: BUILD SUCCESS (or fix any compilation errors)

- [ ] **Step 2: Run tests**

Run: `mvn test`
Expected: Tests pass

- [ ] **Step 3: Commit final**

```bash
git add .
git commit -m "feat: implement JWT security with refresh tokens"
```

---

**Plan complete.** The implementation adds JWT-based stateless authentication with CustomUserDetails, AuthService orchestrator, JwtAuthenticationFilter, and updated SecurityConfig.
# JWT Security Architecture Design

## Overview
This document specifies the design for implementing full JWT-based stateless authentication and authorization using Spring Security, Custom UserDetails, and a Refresh Token rotation strategy.

## Architecture

### 1. Domain & Persistence
- **UserEntity & UserDetails:**
  - `CustomUserDetails` implements `org.springframework.security.core.userdetails.UserDetails`. It will wrap `UserEntity` (or `User`), exposing the email as the `username` and mapping the `UserRole` enum to Spring Security's `GrantedAuthority`.
- **RefreshTokenEntity:**
  - Already exists. Stores a uniquely generated token string mapped to a `UserEntity` with an `expiryDate`.

### 2. Services
- **CustomUserDetailsService:**
  - Implements `UserDetailsService`.
  - Override `loadUserByUsername(String username)` to fetch the `UserEntity` by email from `UserRepository`. If not found, throw `UsernameNotFoundException`.
- **JwtService:**
  - Exists but will be fine-tuned to accept `CustomUserDetails` when generating tokens.
  - Extracts claims and validates token expiration.
- **RefreshTokenService:**
  - Generates, persists, validates, and deletes refresh tokens in the database.
- **AuthService:**
  - Acts as the orchestrator for authentication flows.
  - `register(RegisterUserRequest)`: Maps the request to a new `UserEntity`, saves it, generates a JWT and a Refresh Token, returning them in a `JwtAuthResponse`.
  - `login(LoginUserRequest)`: Uses Spring's `AuthenticationManager` to verify credentials. Upon success, generates a new JWT and a Refresh Token, returning them in a `JwtAuthResponse`.
  - `refreshToken(TokenRefreshRequest)`: Validates the provided refresh token via `RefreshTokenService`, finds the associated user, and issues a new JWT, returning it.
  - `logout(String refreshToken)`: Deletes the provided refresh token from the database.

### 3. Filters & Security Configuration
- **JwtAuthenticationFilter:**
  - A custom filter extending `OncePerRequestFilter`.
  - Extracts the `Authorization` header (`Bearer <token>`).
  - Uses `JwtService` to validate the token.
  - On valid token, invokes `CustomUserDetailsService` to load `CustomUserDetails`.
  - Instantiates a `UsernamePasswordAuthenticationToken` and sets it in the `SecurityContextHolder`.
- **SecurityConfig:**
  - Configures `SessionCreationPolicy.STATELESS`.
  - Disables CSRF.
  - Uses `authorizeHttpRequests` to set `/api/auth/**`, `/v3/api-docs/**`, `/swagger-ui/**`, and `/swagger-ui.html` to `permitAll()`.
  - Sets `.anyRequest().authenticated()` to enforce authentication on all other endpoints.
  - Configures the `JwtAuthenticationFilter` to be executed before `UsernamePasswordAuthenticationFilter`.
  - Exports the `AuthenticationManager` Bean.

### 4. API Responses
- `JwtAuthResponse`:
  - A DTO containing `accessToken` (String), `refreshToken` (String), and `tokenType` (String, default "Bearer").

## Trade-offs and Considerations
- **Stateless Session vs Refresh Token State:** The JWT access token is fully stateless, improving scalability. The Refresh Token is stateful (stored in the database) which allows for server-side token revocation (e.g., during logout).
- **Role-based Access Control (RBAC):** Prepared for future implementation by mapping `UserRole` to `GrantedAuthority`. Further endpoint restriction can be done via `@PreAuthorize` or modifying `authorizeHttpRequests`.

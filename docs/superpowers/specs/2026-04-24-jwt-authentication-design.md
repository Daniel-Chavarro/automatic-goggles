# JWT Authentication & UserDetails Design

## 1. Overview
This document outlines the architecture for integrating Spring Security into the application using a stateless JWT authentication strategy combined with database-backed refresh tokens, maintaining the existing separation between the application's Domain (`User`) and Persistence (`UserEntity`) layers.

## 2. Architecture & Components

### 2.1 UserDetails Implementation (Domain Wrapper)
We use a wrapper approach to map the existing Domain model to Spring Security concepts without polluting the entity layer.
* **`CustomUserDetails`**: A class implementing Spring Security's `UserDetails`. It wraps the domain `User` model, exposing its `email` (as username), `password`, and mapping the domain `UserRole` to a Spring Security `GrantedAuthority`.
* **`CustomUserDetailsService`**: Implements `UserDetailsService`. It injects the `UserService`, retrieves the `User` domain model by email/username, and returns it wrapped in a `CustomUserDetails` instance.

### 2.2 JWT Storage & Token Flow
To avoid database hits on every request while still supporting explicit logout, we use a two-token system:
* **Access Token**: Short-lived JWT (e.g., 15 minutes). Sent as a `Bearer` token in the `Authorization` header. It is completely stateless, meaning it is not stored in the database and is validated solely by verifying its signature.
* **Refresh Token**: Long-lived token (e.g., 7 days). It is generated alongside the Access Token and **stored in the database** via a new `RefreshTokenEntity` (linked to `UserEntity`).

**Data Flow:**
1. **Login**: Client submits credentials. Server validates, generates both tokens, saves the Refresh Token in the DB, and returns both to the client.
2. **Authenticated Requests**: Client sends the Access Token. Server validates it statelessly.
3. **Token Refresh**: Client detects an expired Access Token and submits the Refresh Token to a `/auth/refresh` endpoint. Server validates the Refresh Token in the DB (checking existence and expiry) and issues a new Access Token.
4. **Logout**: Client submits a logout request. Server deletes the corresponding Refresh Token from the DB, terminating the session.

### 2.3 Spring Security Configuration
* **`JwtAuthenticationFilter`**: Intercepts HTTP requests, extracts the Access Token from the headers, uses `JwtService` to validate it, and populates the `SecurityContextHolder` with the authenticated `CustomUserDetails`.
* **`JwtService`**: A service responsible for JWT generation (with specified claims and expiration times), signature verification, and claims extraction.
* **`SecurityConfig`**: Configures Spring Security to be `STATELESS` (disabling HTTP sessions), registers `JwtAuthenticationFilter` before the `UsernamePasswordAuthenticationFilter`, and secures API endpoints.

## 3. Data Models

### 3.1 RefreshTokenEntity
A new persistence entity representing the refresh tokens.
* `id`: UUID (Primary Key)
* `token`: String (Unique, non-nullable)
* `expiryDate`: Instant (The timestamp when this refresh token expires)
* `user`: ManyToOne relation to `UserEntity`

## 4. API Endpoints
* `POST /auth/login`: Accepts credentials, returns Access & Refresh tokens.
* `POST /auth/refresh`: Accepts a Refresh token, returns a new Access token.
* `POST /auth/logout`: Accepts a Refresh token or Bearer token, invalidates the Refresh token in the DB.

## 5. Error Handling & Edge Cases
* **Expired Access Token**: Returns HTTP 401 Unauthorized. Client is expected to call `/auth/refresh`.
* **Invalid/Expired Refresh Token**: Returns HTTP 403 Forbidden. Client must re-authenticate (login again).
* **Logout with Invalid Token**: Fails silently or returns 200 OK to prevent token enumeration.

# Authentication & Authorization

<cite>
**Referenced Files in This Document**
- [SecurityConfig.java](file://admin-backend/src/main/java/com/qhiot/survey/security/SecurityConfig.java)
- [JwtAuthenticationFilter.java](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java)
- [CustomUserDetailsService.java](file://admin-backend/src/main/java/com/qhiot/survey/security/CustomUserDetailsService.java)
- [LoginUser.java](file://admin-backend/src/main/java/com/qhiot/survey/security/LoginUser.java)
- [JwtUtil.java](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java)
- [AuthController.java](file://admin-backend/src/main/java/com/qhiot/survey/controller/AuthController.java)
- [CollabSecurityService.java](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java)
- [CollabEntryController.java](file://admin-backend/src/main/java/com/qhiot/survey/controller/CollabEntryController.java)
- [PasswordConfig.java](file://admin-backend/src/main/java/com/qhiot/survey/config/PasswordConfig.java)
- [SysRole.java](file://admin-backend/src/main/java/com/qhiot/survey/entity/SysRole.java)
- [SysPermission.java](file://admin-backend/src/main/java/com/qhiot/survey/entity/SysPermission.java)
- [Permissions.java](file://admin-backend/src/main/java/com/qhiot/survey/common/constant/Permissions.java)
- [PermissionInitializer.java](file://admin-backend/src/main/java/com/qhiot/survey/common/init/PermissionInitializer.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Project Structure](#project-structure)
3. [Core Components](#core-components)
4. [Architecture Overview](#architecture-overview)
5. [Detailed Component Analysis](#detailed-component-analysis)
6. [Dependency Analysis](#dependency-analysis)
7. [Performance Considerations](#performance-considerations)
8. [Troubleshooting Guide](#troubleshooting-guide)
9. [Conclusion](#conclusion)
10. [Appendices](#appendices)

## Introduction
This document explains the authentication and authorization system of the Survey App backend. It covers JWT-based authentication, token generation and validation, refresh mechanisms, user roles and permissions, and the collaboration token system for cross-platform access control. It also documents the security filter chain, custom user details service, password encoding strategy, and practical examples of authentication flows and permission-based access control.

## Project Structure
The authentication subsystem is primarily located under the admin-backend module:
- Security configuration and filters: security package
- JWT utilities: common/util
- Controllers: controller package (AuthController, CollabEntryController)
- Entities and permissions: entity package
- Password encoding: config package
- Permission initialization: common/init

```mermaid
graph TB
subgraph "Security Layer"
SC["SecurityConfig.java"]
JAF["JwtAuthenticationFilter.java"]
CUDS["CustomUserDetailsService.java"]
LUSER["LoginUser.java"]
PASSCFG["PasswordConfig.java"]
end
subgraph "JWT Utilities"
JWTU["JwtUtil.java"]
end
subgraph "Controllers"
AUTHC["AuthController.java"]
COLC["CollabEntryController.java"]
end
subgraph "Domain Models"
ROLE["SysRole.java"]
PERM["SysPermission.java"]
PERMS["Permissions.java"]
end
subgraph "Init"
PERMINIT["PermissionInitializer.java"]
end
SC --> JAF
JAF --> JWTU
JAF --> CUDS
CUDS --> ROLE
AUTHC --> JWTU
AUTHC --> PASSCFG
COLC --> JWTU
PERMINIT --> PERM
PERMINIT --> PERMS
```

**Diagram sources**
- [SecurityConfig.java:1-99](file://admin-backend/src/main/java/com/qhiot/survey/security/SecurityConfig.java#L1-L99)
- [JwtAuthenticationFilter.java:1-135](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L1-L135)
- [CustomUserDetailsService.java:1-91](file://admin-backend/src/main/java/com/qhiot/survey/security/CustomUserDetailsService.java#L1-L91)
- [LoginUser.java:1-36](file://admin-backend/src/main/java/com/qhiot/survey/security/LoginUser.java#L1-L36)
- [JwtUtil.java:1-174](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L1-L174)
- [AuthController.java:1-552](file://admin-backend/src/main/java/com/qhiot/survey/controller/AuthController.java#L1-L552)
- [CollabEntryController.java:1-89](file://admin-backend/src/main/java/com/qhiot/survey/controller/CollabEntryController.java#L1-L89)
- [SysRole.java:1-40](file://admin-backend/src/main/java/com/qhiot/survey/entity/SysRole.java#L1-L40)
- [SysPermission.java:1-56](file://admin-backend/src/main/java/com/qhiot/survey/entity/SysPermission.java#L1-L56)
- [Permissions.java:1-81](file://admin-backend/src/main/java/com/qhiot/survey/common/constant/Permissions.java#L1-L81)
- [PermissionInitializer.java:1-38](file://admin-backend/src/main/java/com/qhiot/survey/common/init/PermissionInitializer.java#L1-L38)

**Section sources**
- [SecurityConfig.java:1-99](file://admin-backend/src/main/java/com/qhiot/survey/security/SecurityConfig.java#L1-L99)
- [JwtAuthenticationFilter.java:1-135](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L1-L135)
- [JwtUtil.java:1-174](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L1-L174)
- [AuthController.java:1-552](file://admin-backend/src/main/java/com/qhiot/survey/controller/AuthController.java#L1-L552)
- [CollabSecurityService.java:1-126](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L1-L126)
- [CollabEntryController.java:1-89](file://admin-backend/src/main/java/com/qhiot/survey/controller/CollabEntryController.java#L1-L89)
- [CustomUserDetailsService.java:1-91](file://admin-backend/src/main/java/com/qhiot/survey/security/CustomUserDetailsService.java#L1-L91)
- [LoginUser.java:1-36](file://admin-backend/src/main/java/com/qhiot/survey/security/LoginUser.java#L1-L36)
- [PasswordConfig.java:1-18](file://admin-backend/src/main/java/com/qhiot/survey/config/PasswordConfig.java#L1-L18)
- [SysRole.java:1-40](file://admin-backend/src/main/java/com/qhiot/survey/entity/SysRole.java#L1-L40)
- [SysPermission.java:1-56](file://admin-backend/src/main/java/com/qhiot/survey/entity/SysPermission.java#L1-L56)
- [Permissions.java:1-81](file://admin-backend/src/main/java/com/qhiot/survey/common/constant/Permissions.java#L1-L81)
- [PermissionInitializer.java:1-38](file://admin-backend/src/main/java/com/qhiot/survey/common/init/PermissionInitializer.java#L1-L38)

## Core Components
- Security filter chain: Stateless JWT filter plus CORS and encoding configuration.
- JWT utilities: token generation, parsing, validation, and claims extraction.
- Authentication controller: login, SMS login, refresh, logout, user info retrieval.
- Collaboration security service: validates collaboration entries, applies whitelist-based access control, and logs access.
- Custom user details service: loads user roles and permissions, expands wildcards, and builds authorities.
- Password encoding: BCrypt encoder configured via PasswordConfig.
- Role and permission model: SysRole with JSON-encoded permissions and SysPermission for metadata.

**Section sources**
- [SecurityConfig.java:39-61](file://admin-backend/src/main/java/com/qhiot/survey/security/SecurityConfig.java#L39-L61)
- [JwtUtil.java:34-51](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L34-L51)
- [AuthController.java:139-238](file://admin-backend/src/main/java/com/qhiot/survey/controller/AuthController.java#L139-L238)
- [CollabSecurityService.java:26-105](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L26-L105)
- [CustomUserDetailsService.java:31-89](file://admin-backend/src/main/java/com/qhiot/survey/security/CustomUserDetailsService.java#L31-L89)
- [PasswordConfig.java:14-17](file://admin-backend/src/main/java/com/qhiot/survey/config/PasswordConfig.java#L14-L17)
- [SysRole.java:25-28](file://admin-backend/src/main/java/com/qhiot/survey/entity/SysRole.java#L25-L28)
- [SysPermission.java:24-30](file://admin-backend/src/main/java/com/qhiot/survey/entity/SysPermission.java#L24-L30)

## Architecture Overview
The authentication and authorization architecture centers on a stateless JWT filter that authenticates requests and delegates collaboration tokens to a specialized security service. Access control is enforced via method-level security and request whitelists for collaboration.

```mermaid
sequenceDiagram
participant Client as "Client"
participant Sec as "SecurityFilterChain"
participant Filter as "JwtAuthenticationFilter"
participant JWT as "JwtUtil"
participant UDS as "CustomUserDetailsService"
participant CSec as "CollabSecurityService"
participant Ctrl as "AuthController"
Client->>Sec : "HTTP Request with Authorization : Bearer ..."
Sec->>Filter : "doFilterInternal(...)"
Filter->>JWT : "validateToken(token)"
alt "Collaboration Token"
Filter->>CSec : "loadValidEntry(entryId)"
CSec-->>Filter : "CollabEntry or null"
Filter->>CSec : "isAccessAllowed(request)"
CSec-->>Filter : "true/false"
Filter-->>Client : "200 or 401/403"
else "Internal Token"
Filter->>JWT : "getUsernameFromToken(token)"
Filter->>UDS : "loadUserByUsername(username)"
UDS-->>Filter : "UserDetails with authorities"
Filter-->>Client : "Continue with authenticated context"
end
Client->>Ctrl : "Subsequent requests (optional)"
```

**Diagram sources**
- [SecurityConfig.java:40-61](file://admin-backend/src/main/java/com/qhiot/survey/security/SecurityConfig.java#L40-L61)
- [JwtAuthenticationFilter.java:44-81](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L44-L81)
- [JwtUtil.java:154-161](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L154-L161)
- [CustomUserDetailsService.java:31-89](file://admin-backend/src/main/java/com/qhiot/survey/security/CustomUserDetailsService.java#L31-L89)
- [CollabSecurityService.java:39-105](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L39-L105)
- [AuthController.java:139-238](file://admin-backend/src/main/java/com/qhiot/survey/controller/AuthController.java#L139-L238)

## Detailed Component Analysis

### Security Filter Chain and CORS
- CSRF disabled; form and basic auth disabled; session policy stateless.
- Public endpoints permitted: authentication, health, Swagger, actuator, and public API.
- JWT filter added before username/password filter.
- Character encoding filter ensures UTF-8.
- CORS allows credentials and exposes Authorization header; origin patterns handled safely.

**Section sources**
- [SecurityConfig.java:40-61](file://admin-backend/src/main/java/com/qhiot/survey/security/SecurityConfig.java#L40-L61)
- [SecurityConfig.java:68-97](file://admin-backend/src/main/java/com/qhiot/survey/security/SecurityConfig.java#L68-L97)

### JWT Utilities: Generation, Validation, and Claims
- Access tokens and refresh tokens generated with HS256 using a configurable secret and expiration.
- Collaboration tokens carry loginType=collab and collabEntryId, enabling cross-platform access control.
- Claims extraction supports userId, username, tokenType, loginType, and collabEntryId.
- Token validation checks signature and expiration.

```mermaid
flowchart TD
Start(["Generate Token"]) --> BuildClaims["Build Claims<br/>userId, username, tokenType/loginType, TTL"]
BuildClaims --> Sign["Sign with HS256 using secret"]
Sign --> Return(["Return JWT"])
Return --> Validate["validateToken(token)"]
Validate --> Parse["parseClaimsJws(token)"]
Parse --> Extract["Extract Claims<br/>userId, username, tokenType, loginType, collabEntryId"]
```

**Diagram sources**
- [JwtUtil.java:34-51](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L34-L51)
- [JwtUtil.java:73-85](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L73-L85)
- [JwtUtil.java:154-161](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L154-L161)
- [JwtUtil.java:102-149](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L102-L149)

**Section sources**
- [JwtUtil.java:22-29](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L22-L29)
- [JwtUtil.java:34-51](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L34-L51)
- [JwtUtil.java:102-149](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L102-L149)
- [JwtUtil.java:154-161](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L154-L161)

### Authentication Controller: Login, Refresh, Logout, and User Info
- Captcha endpoint stores 4-digit code in Redis with short TTL for verification.
- Username/password login validates captcha, checks user status and lockout, authenticates via AuthenticationManager, generates access and refresh tokens, and records login logs.
- SMS login verifies SMS code, resolves user by phone, generates tokens, updates last login, and logs.
- Refresh endpoint validates refresh token type and expiration, reissues new tokens for the user.
- Change password and reset password endpoints use PasswordEncoder and update user records.
- Logout clears SecurityContext.
- getUserInfo aggregates roles and permissions for the current user.

```mermaid
sequenceDiagram
participant Client as "Client"
participant Auth as "AuthController"
participant Redis as "StringRedisTemplate"
participant AM as "AuthenticationManager"
participant JWT as "JwtUtil"
participant Log as "LoginLogService"
Client->>Auth : "POST /auth/login (username/password + captchaKey + captcha)"
Auth->>Redis : "GET captcha : <key>"
Redis-->>Auth : "code or null"
Auth->>AM : "authenticate(UsernamePasswordAuthenticationToken)"
AM-->>Auth : "Authentication"
Auth->>JWT : "generateAccessToken(userId, username, 'internal')"
Auth->>JWT : "generateRefreshToken(userId, username)"
Auth->>Log : "logLogin(...)"
Auth-->>Client : "LoginResponse {accessToken, refreshToken, roles, permissions}"
```

**Diagram sources**
- [AuthController.java:139-238](file://admin-backend/src/main/java/com/qhiot/survey/controller/AuthController.java#L139-L238)
- [JwtUtil.java:34-51](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L34-L51)

**Section sources**
- [AuthController.java:139-238](file://admin-backend/src/main/java/com/qhiot/survey/controller/AuthController.java#L139-L238)
- [AuthController.java:399-427](file://admin-backend/src/main/java/com/qhiot/survey/controller/AuthController.java#L399-L427)
- [AuthController.java:481-550](file://admin-backend/src/main/java/com/qhiot/survey/controller/AuthController.java#L481-L550)

### Collaboration Token System
- Collaboration entries define a bounded scope and TTL.
- CollabSecurityService validates entries (status and expiry) and enforces a strict whitelist for allowed GET endpoints.
- JwtAuthenticationFilter recognizes loginType=collab and delegates to CollabSecurityService for authentication and access control.
- Access logs are recorded for each collaboration request.

```mermaid
flowchart TD
A["Receive Bearer collab token"] --> B["JwtUtil.getLoginType(token) == 'collab'?"]
B --> |Yes| C["JwtUtil.getCollabEntryIdFromToken(token)"]
C --> D["CollabSecurityService.loadValidEntry(entryId)"]
D --> E{"Entry valid?"}
E --> |No| F["401 Unauthorized + logAccess"]
E --> |Yes| G["CollabSecurityService.isAccessAllowed(request)"]
G --> H{"Allowed?"}
H --> |No| I["403 Forbidden + logAccess"]
H --> |Yes| J["Set ROLE_COLLAB + pass through + logAccess"]
B --> |No| K["Standard internal token flow"]
```

**Diagram sources**
- [JwtAuthenticationFilter.java:49-122](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L49-L122)
- [JwtUtil.java:126-149](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L126-L149)
- [CollabSecurityService.java:39-105](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L39-L105)

**Section sources**
- [JwtAuthenticationFilter.java:27-32](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L27-L32)
- [JwtAuthenticationFilter.java:86-122](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L86-L122)
- [CollabSecurityService.java:28-31](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L28-L31)
- [CollabSecurityService.java:66-105](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L66-L105)
- [CollabEntryController.java:83-88](file://admin-backend/src/main/java/com/qhiot/survey/controller/CollabEntryController.java#L83-L88)

### Custom User Details Service and Authorities
- Loads user by username, checks status, and retrieves assigned roles.
- Builds role authorities (ROLE_<CODE>) and aggregates raw permission codes from roles.
- Expands wildcard permissions via PermissionRegistry and ensures at least ROLE_USER if empty.
- Returns a LoginUser with authorities for downstream authentication.

```mermaid
classDiagram
class CustomUserDetailsService {
+loadUserByUsername(username) UserDetails
}
class LoginUser {
+Long userId
+String realName
}
class SysRole {
+String roleCode
+String permissions
}
CustomUserDetailsService --> SysRole : "loads roles"
CustomUserDetailsService --> LoginUser : "returns"
```

**Diagram sources**
- [CustomUserDetailsService.java:31-89](file://admin-backend/src/main/java/com/qhiot/survey/security/CustomUserDetailsService.java#L31-L89)
- [LoginUser.java:14-35](file://admin-backend/src/main/java/com/qhiot/survey/security/LoginUser.java#L14-L35)
- [SysRole.java:21-28](file://admin-backend/src/main/java/com/qhiot/survey/entity/SysRole.java#L21-L28)

**Section sources**
- [CustomUserDetailsService.java:31-89](file://admin-backend/src/main/java/com/qhiot/survey/security/CustomUserDetailsService.java#L31-L89)
- [LoginUser.java:14-35](file://admin-backend/src/main/java/com/qhiot/survey/security/LoginUser.java#L14-L35)
- [SysRole.java:21-28](file://admin-backend/src/main/java/com/qhiot/survey/entity/SysRole.java#L21-L28)

### Password Encoding Strategy
- BCryptPasswordEncoder bean is registered for secure password hashing.
- Used during login credential checks and password updates/reset.

**Section sources**
- [PasswordConfig.java:14-17](file://admin-backend/src/main/java/com/qhiot/survey/config/PasswordConfig.java#L14-L17)
- [AuthController.java:350-363](file://admin-backend/src/main/java/com/qhiot/survey/controller/AuthController.java#L350-L363)
- [AuthController.java:386-390](file://admin-backend/src/main/java/com/qhiot/survey/controller/AuthController.java#L386-L390)

### Roles, Permissions, and Method-Level Security
- Permissions are declared as constants and synchronized to the database at startup.
- Roles carry JSON-encoded permission lists; authorities are expanded and merged with role prefixes.
- Method-level security is enabled; permissions are enforced via hasAuthority expressions and UI directives.

```mermaid
graph LR
PERMS["Permissions.java"] --> REG["PermissionRegistry (via initializer)"]
REG --> DB["SysPermission (sync)"]
ROLE["SysRole.permissions (JSON array)"] --> EXPAND["Expand wildcards"]
EXPAND --> AUTH["Authorities (ROLE_* + perms)"]
AUTH --> SEC["@PreAuthorize / UI v-permission"]
```

**Diagram sources**
- [Permissions.java:13-80](file://admin-backend/src/main/java/com/qhiot/survey/common/constant/Permissions.java#L13-L80)
- [PermissionInitializer.java:22-36](file://admin-backend/src/main/java/com/qhiot/survey/common/init/PermissionInitializer.java#L22-L36)
- [SysPermission.java:24-30](file://admin-backend/src/main/java/com/qhiot/survey/entity/SysPermission.java#L24-L30)
- [SysRole.java:25-28](file://admin-backend/src/main/java/com/qhiot/survey/entity/SysRole.java#L25-L28)
- [CustomUserDetailsService.java:64-80](file://admin-backend/src/main/java/com/qhiot/survey/security/CustomUserDetailsService.java#L64-L80)

**Section sources**
- [Permissions.java:13-80](file://admin-backend/src/main/java/com/qhiot/survey/common/constant/Permissions.java#L13-L80)
- [PermissionInitializer.java:22-36](file://admin-backend/src/main/java/com/qhiot/survey/common/init/PermissionInitializer.java#L22-L36)
- [SysPermission.java:24-30](file://admin-backend/src/main/java/com/qhiot/survey/entity/SysPermission.java#L24-L30)
- [SysRole.java:25-28](file://admin-backend/src/main/java/com/qhiot/survey/entity/SysRole.java#L25-L28)
- [CustomUserDetailsService.java:64-80](file://admin-backend/src/main/java/com/qhiot/survey/security/CustomUserDetailsService.java#L64-L80)

### Permission Matrix Examples
- Project management: project:view, project:edit, template:bind.
- Point management: point:view, point:edit.
- Survey management: survey:create, survey:edit, survey:submit, survey:assist.
- Audit management: audit:view, audit:pass, audit:reject.
- System operations: system:log.
- Export capabilities: export:project, export:audit.

These are enforced via hasAuthority checks and UI permission directives.

**Section sources**
- [Permissions.java:16-78](file://admin-backend/src/main/java/com/qhiot/survey/common/constant/Permissions.java#L16-L78)

## Dependency Analysis
The authentication stack exhibits low coupling and clear separation of concerns:
- SecurityConfig depends on JwtAuthenticationFilter.
- JwtAuthenticationFilter depends on JwtUtil, UserDetailsService, and CollabSecurityService.
- CustomUserDetailsService depends on SysUserService and SysRoleService to assemble authorities.
- AuthController orchestrates authentication flows and integrates with JWT utilities and services.
- CollabEntryController manages collaboration lifecycle and token issuance.

```mermaid
graph TB
SC["SecurityConfig"] --> JAF["JwtAuthenticationFilter"]
JAF --> JWTU["JwtUtil"]
JAF --> CUDS["CustomUserDetailsService"]
JAF --> CSEC["CollabSecurityService"]
AUTHC["AuthController"] --> JWTU
AUTHC --> PASSCFG["PasswordConfig"]
PERMINIT["PermissionInitializer"] --> PERM["SysPermission"]
PERMINIT --> PERMS["Permissions"]
```

**Diagram sources**
- [SecurityConfig.java:34-36](file://admin-backend/src/main/java/com/qhiot/survey/security/SecurityConfig.java#L34-L36)
- [JwtAuthenticationFilter.java:39-41](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L39-L41)
- [JwtUtil.java:1-174](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L1-L174)
- [CustomUserDetailsService.java:28-29](file://admin-backend/src/main/java/com/qhiot/survey/security/CustomUserDetailsService.java#L28-L29)
- [CollabSecurityService.java:33-34](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L33-L34)
- [AuthController.java:52-59](file://admin-backend/src/main/java/com/qhiot/survey/controller/AuthController.java#L52-L59)
- [PasswordConfig.java:14-17](file://admin-backend/src/main/java/com/qhiot/survey/config/PasswordConfig.java#L14-L17)
- [PermissionInitializer.java:20-36](file://admin-backend/src/main/java/com/qhiot/survey/common/init/PermissionInitializer.java#L20-L36)

**Section sources**
- [SecurityConfig.java:34-36](file://admin-backend/src/main/java/com/qhiot/survey/security/SecurityConfig.java#L34-L36)
- [JwtAuthenticationFilter.java:39-41](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L39-L41)
- [AuthController.java:52-59](file://admin-backend/src/main/java/com/qhiot/survey/controller/AuthController.java#L52-L59)

## Performance Considerations
- Stateless JWT eliminates server-side session storage overhead.
- Token validation relies on symmetric signing; keep secret secure and rotate periodically.
- Collaboration access logging writes to database per request; consider batching or sampling in high-throughput scenarios.
- Role and permission expansion occurs per request; caching user authorities in Redis could reduce CPU load for frequent access.

[No sources needed since this section provides general guidance]

## Troubleshooting Guide
Common issues and resolutions:
- Unauthorized responses during JWT filter execution: verify token presence, format, and expiration; check filter error handling path.
- Collaboration token denied: confirm entry validity, whitelist allowance, and collabEntryId correctness.
- Refresh token invalid: ensure tokenType equals "refresh" and not expired; verify user exists and is active.
- User disabled or locked: login attempts will fail; unlock or reactivate the user.
- CORS errors: verify allowed origins pattern and credentials exposure.

**Section sources**
- [JwtAuthenticationFilter.java:72-78](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L72-L78)
- [JwtAuthenticationFilter.java:92-98](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L92-L98)
- [AuthController.java:402-408](file://admin-backend/src/main/java/com/qhiot/survey/controller/AuthController.java#L402-L408)
- [SecurityConfig.java:68-89](file://admin-backend/src/main/java/com/qhiot/survey/security/SecurityConfig.java#L68-L89)

## Conclusion
The system implements a robust, stateless JWT-based authentication and authorization framework with clear separation of concerns. Internal users authenticate via username/password or SMS, receive access and refresh tokens, and are authorized by role and permission matrices. Collaboration tokens enable controlled third-party access with strict whitelisting and auditing. The design leverages Spring Security’s method-level security and a custom user details service to deliver flexible and secure access control suitable for production environments.

[No sources needed since this section summarizes without analyzing specific files]

## Appendices

### Example Authentication Flows

- Standard login flow:
  - Obtain captcha, submit username/password with captcha.
  - On success, receive access and refresh tokens.
  - Use access token for subsequent requests; refresh when needed.

- Collaboration token flow:
  - Create or manage a collaboration entry.
  - Issue a collab token bound to the entry.
  - Third-party client uses the token to access whitelisted endpoints.

- Permission-based access control:
  - Annotate endpoints with method-level security using hasAuthority.
  - Frontend uses permission directives to hide unauthorized UI elements.

**Section sources**
- [AuthController.java:139-238](file://admin-backend/src/main/java/com/qhiot/survey/controller/AuthController.java#L139-L238)
- [AuthController.java:399-427](file://admin-backend/src/main/java/com/qhiot/survey/controller/AuthController.java#L399-L427)
- [CollabEntryController.java:83-88](file://admin-backend/src/main/java/com/qhiot/survey/controller/CollabEntryController.java#L83-L88)
- [Permissions.java:16-78](file://admin-backend/src/main/java/com/qhiot/survey/common/constant/Permissions.java#L16-L78)
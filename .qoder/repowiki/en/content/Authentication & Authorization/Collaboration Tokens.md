# Collaboration Tokens

<cite>
**Referenced Files in This Document**
- [JwtAuthenticationFilter.java](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java)
- [CollabSecurityService.java](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java)
- [JwtUtil.java](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java)
- [CollabEntryController.java](file://admin-backend/src/main/java/com/qhiot/survey/controller/CollabEntryController.java)
- [CollabEntryService.java](file://admin-backend/src/main/java/com/qhiot/survey/service/CollabEntryService.java)
- [CollabEntryServiceImpl.java](file://admin-backend/src/main/java/com/qhiot/survey/service/impl/CollabEntryServiceImpl.java)
- [CollabEntry.java](file://admin-backend/src/main/java/com/qhiot/survey/entity/CollabEntry.java)
- [CollabAccessLog.java](file://admin-backend/src/main/java/com/qhiot/survey/entity/CollabAccessLog.java)
- [CollabEntryMapper.java](file://admin-backend/src/main/java/com/qhiot/survey/mapper/CollabEntryMapper.java)
- [CollabAccessLogMapper.java](file://admin-backend/src/main/java/com/qhiot/survey/mapper/CollabAccessLogMapper.java)
- [Permissions.java](file://admin-backend/src/main/java/com/qhiot/survey/common/constant/Permissions.java)
- [01-init.sql](file://admin-backend/init-data/01-init.sql)
- [application.yml](file://admin-backend/src/main/resources/application.yml)
- [CollabTokenSecurityTest.java](file://admin-backend/src/test/java/com/qhiot/survey/security/CollabTokenSecurityTest.java)
</cite>

## Update Summary
**Changes Made**
- Enhanced collaboration security service with comprehensive white-list based access control
- Implemented detailed collaborative token support with dedicated ROLE_COLLAB role
- Added comprehensive access logging system for third-party collaborator monitoring
- Strengthened endpoint restrictions with explicit blacklist and whitelist policies
- Expanded permission-based access control with object-level scope enforcement

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
This document explains the enhanced collaboration token system that enables controlled, cross-platform access to survey-related resources with comprehensive security policies. The system now features white-list based access control, collaborative token support, and detailed access logging for third-party collaborators. The architecture provides robust security controls with explicit endpoint restrictions, object-level scope enforcement, and comprehensive audit trails for monitoring collaboration activities across external platforms.

## Project Structure
The collaboration token system spans security filters, services, controllers, persistence, and tests. The backend module organizes these concerns by package:
- security: authentication filter and enhanced collaboration-specific security logic with white-list access control
- service and impl: business logic for collaboration entries and token issuance with permission validation
- controller: REST endpoints for managing collaboration entries and issuing tokens
- entity and mapper: persistence models and MyBatis mappers
- common/util: JWT utilities with enhanced token generation
- common/constant: permission code definitions for granular access control
- init-data: database schema initialization
- resources: runtime configuration (JWT secrets and expiration)

```mermaid
graph TB
subgraph "Security Layer"
F["JwtAuthenticationFilter"]
S["CollabSecurityService"]
U["JwtUtil"]
P["Permissions"]
end
subgraph "Service Layer"
CEC["CollabEntryController"]
CES["CollabEntryService"]
CEI["CollabEntryServiceImpl"]
end
subgraph "Persistence"
M1["CollabEntryMapper"]
M2["CollabAccessLogMapper"]
E1["CollabEntry"]
E2["CollabAccessLog"]
end
subgraph "Runtime Config"
CFG["application.yml"]
end
F --> S
F --> U
S --> P
CEC --> CES
CES --> CEI
CEI --> M1
CEI --> M2
CEI --> U
S --> M1
S --> M2
CFG --> U
```

**Diagram sources**
- [JwtAuthenticationFilter.java:39-41](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L39-L41)
- [CollabSecurityService.java:38-45](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L38-L45)
- [JwtUtil.java:22-29](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L22-L29)
- [Permissions.java:9-114](file://admin-backend/src/main/java/com/qhiot/survey/common/constant/Permissions.java#L9-L114)
- [CollabEntryController.java:24-25](file://admin-backend/src/main/java/com/qhiot/survey/controller/CollabEntryController.java#L24-L25)
- [CollabEntryServiceImpl.java:28-32](file://admin-backend/src/main/java/com/qhiot/survey/service/impl/CollabEntryServiceImpl.java#L28-L32)
- [CollabEntryMapper.java:1-9](file://admin-backend/src/main/java/com/qhiot/survey/mapper/CollabEntryMapper.java#L1-L9)
- [CollabAccessLogMapper.java:1-12](file://admin-backend/src/main/java/com/qhiot/survey/mapper/CollabAccessLogMapper.java#L1-L12)
- [application.yml:9-13](file://admin-backend/src/main/resources/application.yml#L9-L13)

**Section sources**
- [JwtAuthenticationFilter.java:39-41](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L39-L41)
- [CollabEntryController.java:24-25](file://admin-backend/src/main/java/com/qhiot/survey/controller/CollabEntryController.java#L24-L25)
- [CollabEntryServiceImpl.java:28-32](file://admin-backend/src/main/java/com/qhiot/survey/service/impl/CollabEntryServiceImpl.java#L28-L32)
- [01-init.sql:212-229](file://admin-backend/init-data/01-init.sql#L212-L229)
- [application.yml:9-13](file://admin-backend/src/main/resources/application.yml#L9-L13)

## Core Components
- **JwtAuthenticationFilter**: Central filter that detects collaboration tokens (loginType=collab), validates the associated entry, applies white-list/blacklist access control with object-level scope enforcement, sets a dedicated ROLE_COLLAB, and writes comprehensive access logs.
- **CollabSecurityService**: Validates collaboration entries (enabled and not expired), enforces comprehensive access policies including whitelist/blacklist and object-level permissions, and logs detailed access attempts with audit trails.
- **CollabEntryController**: Manages collaboration entries and issues tokens via dedicated endpoints with enhanced validation.
- **CollabEntryService/Impl**: Implements CRUD for entries, token reset, token issuance with TTL calculation, and comprehensive access log retrieval with filtering capabilities.
- **JwtUtil**: Generates collaboration tokens embedding collabEntryId and loginType=collab, and extracts claims for validation.
- **Permissions**: Defines granular permission codes (project:view, point:view, audit:view, etc.) for fine-grained access control.
- **Persistence**: CollabEntry and CollabAccessLog entities backed by CollabEntryMapper and CollabAccessLogMapper with enhanced indexing for performance.
- **Tests**: CollabTokenSecurityTest validates access policy enforcement, object-level scope restrictions, and comprehensive filter behavior under various scenarios.

**Section sources**
- [JwtAuthenticationFilter.java:45-130](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L45-L130)
- [CollabSecurityService.java:47-117](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L47-L117)
- [CollabEntryController.java:19-89](file://admin-backend/src/main/java/com/qhiot/survey/controller/CollabEntryController.java#L19-L89)
- [CollabEntryService.java:9-53](file://admin-backend/src/main/java/com/qhiot/survey/service/CollabEntryService.java#L9-L53)
- [CollabEntryServiceImpl.java:25-142](file://admin-backend/src/main/java/com/qhiot/survey/service/impl/CollabEntryServiceImpl.java#L25-L142)
- [JwtUtil.java:57-68](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L57-L68)
- [Permissions.java:16-112](file://admin-backend/src/main/java/com/qhiot/survey/common/constant/Permissions.java#L16-L112)
- [CollabEntry.java:10-59](file://admin-backend/src/main/java/com/qhiot/survey/entity/CollabEntry.java#L10-L59)
- [CollabAccessLog.java:11-43](file://admin-backend/src/main/java/com/qhiot/survey/entity/CollabAccessLog.java#L11-L43)
- [CollabTokenSecurityTest.java:34-294](file://admin-backend/src/test/java/com/qhiot/survey/security/CollabTokenSecurityTest.java#L34-L294)

## Architecture Overview
The enhanced collaboration token architecture now features comprehensive security policies with white-list based access control for third-party integrations, including object-level scope enforcement and detailed audit logging.

```mermaid
sequenceDiagram
participant Ext as "External Platform"
participant API as "CollabEntryController"
participant Svc as "CollabEntryServiceImpl"
participant Util as "JwtUtil"
participant Sec as "CollabSecurityService"
participant Filt as "JwtAuthenticationFilter"
Ext->>API : "POST /api/v1/collab/{id}/issue-token"
API->>Svc : "issueCollabToken(entryId)"
Svc->>Svc : "validate entry status, TTL, and permissions"
Svc->>Util : "generateCollabToken(entryId, entryName, ttl)"
Util-->>Svc : "collab JWT"
Svc-->>API : "JWT string"
API-->>Ext : "JWT string"
note over Ext,Filt : "Subsequent requests include Authorization : Bearer <JWT>"
Ext->>Filt : "HTTP Request with JWT"
Filt->>Util : "getLoginType(token)"
Util-->>Filt : "loginType=collab"
Filt->>Sec : "loadValidEntry(entryId)"
Sec-->>Filt : "entry or null"
alt "Invalid entry"
Filt->>Sec : "logAccess(entryId, token, request, 401)"
Filt-->>Ext : "401 Unauthorized"
else "Valid entry"
Filt->>Sec : "isAccessAllowed(entry, request)"
Sec->>Sec : "check blacklist + whitelist + object scope"
Sec-->>Filt : "true/false"
alt "Allowed"
Filt->>Filt : "set ROLE_COLLAB + granted permissions"
Filt->>Filt : "proceed to handler"
Filt->>Sec : "logAccess(entryId, token, request, response.status)"
else "Denied"
Filt->>Sec : "logAccess(entryId, token, request, 403)"
Filt-->>Ext : "403 Forbidden"
end
end
```

**Diagram sources**
- [CollabEntryController.java:83-88](file://admin-backend/src/main/java/com/qhiot/survey/controller/CollabEntryController.java#L83-L88)
- [CollabEntryServiceImpl.java:121-141](file://admin-backend/src/main/java/com/qhiot/survey/service/impl/CollabEntryServiceImpl.java#L121-L141)
- [JwtUtil.java:57-68](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L57-L68)
- [JwtAuthenticationFilter.java:88-130](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L88-L130)
- [CollabSecurityService.java:77-117](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L77-L117)

## Detailed Component Analysis

### Enhanced White-List Based Access Control System
The collaboration security service now implements comprehensive access control policies with explicit white-list and black-list enforcement:

- **Entry validation**: Only enabled entries that are not expired are considered valid. Expired or revoked entries cause immediate 401 responses.
- **Black-list enforcement**: Explicitly blocks sensitive operations including audit processing, deletion operations, bulk exports, user/role/system management, dictionaries, and actuator endpoints.
- **White-list enforcement**: Only allows GET requests to specific read-only endpoints for point, result, template, project, section, file, and health services.
- **Method restriction**: All non-GET methods are blocked regardless of endpoint, ensuring write operations are prevented.
- **Object-level scope enforcement**: Validates access against authorized project IDs and point IDs defined in the collaboration entry.
- **Permission-based access control**: Supports granular permissions including project:view, point:view, template:view, and audit:view capabilities.
- **Role assignment**: On successful validation, assigns ROLE_COLLAB with granted permission codes to the security context.
- **Enhanced logging**: Every access attempt is logged with detailed information including IP, user-agent, request path, response code, and authorization scope.

```mermaid
flowchart TD
Start(["Request Received"]) --> GetToken["Extract JWT from Authorization"]
GetToken --> Validate["Validate JWT and parse claims"]
Validate --> IsCollab{"loginType == collab?"}
IsCollab --> |No| InternalFlow["Internal token flow<br/>UserDetailsService + standard auth"] --> End
IsCollab --> |Yes| LoadEntry["loadValidEntry(entryId)"]
LoadEntry --> EntryOK{"Entry valid?"}
EntryOK --> |No| LogFail["logAccess(401)"] --> Respond401["401 Unauthorized"] --> End
EntryOK --> |Yes| CheckBlacklist["Check black-listed endpoints"]
CheckBlacklist --> Blacklisted{"Blacklisted path?"}
Blacklisted --> |Yes| LogDeny["logAccess(403)"] --> Respond403["403 Forbidden"] --> End
Blacklisted --> |No| CheckMethod["Check HTTP method"]
CheckMethod --> DeleteMethod{"DELETE method?"}
DeleteMethod --> |Yes| LogDeny2["logAccess(403)"] --> Respond403 --> End
DeleteMethod --> |No| CheckWhitelist["Check white-listed GET endpoints"]
CheckWhitelist --> Whitelisted{"GET on whitelisted paths?"}
Whitelisted --> |No| LogDeny3["logAccess(403)"] --> Respond403 --> End
Whitelisted --> |Yes| CheckScope["Validate object-level scope<br/>(projectIds, pointIds)"]
CheckScope --> Allowed{"Within authorized scope?"}
Allowed --> |No| LogDeny4["logAccess(403)"] --> Respond403 --> End
Allowed --> |Yes| SetAuth["Set ROLE_COLLAB + granted permissions"] --> Next["Proceed to handler"] --> LogSuccess["logAccess(response.status)"] --> End
```

**Diagram sources**
- [JwtAuthenticationFilter.java:88-130](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L88-L130)
- [CollabSecurityService.java:77-171](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L77-L171)
- [CollabSecurityService.java:257-271](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L257-L271)

**Section sources**
- [CollabSecurityService.java:77-171](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L77-L171)
- [JwtAuthenticationFilter.java:88-130](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L88-L130)
- [CollabTokenSecurityTest.java:107-166](file://admin-backend/src/test/java/com/qhiot/survey/security/CollabTokenSecurityTest.java#L107-L166)

### Token Lifecycle: Enhanced Creation to Expiration
The token lifecycle now includes enhanced validation and permission management:

- **Creation**: Collaboration entries are created with generated token, enabled status, and comprehensive permission configuration including project IDs, point IDs, and permission scopes.
- **Enhanced issuance**: The system generates a collaboration JWT embedding collabEntryId and loginType=collab with TTL derived from entry's expireTime or default 7 days.
- **Advanced validation**: The filter extracts collabEntryId from the token and validates entry status, expiration, and permission scope.
- **Revocation**: Entries can be revoked (status=3), invalidating future access attempts even with unexpired tokens.
- **Reset**: Tokens can be reset immediately invalidating old tokens and generating new ones with updated permissions.

```mermaid
sequenceDiagram
participant Admin as "Admin"
participant Ctl as "CollabEntryController"
participant Svc as "CollabEntryServiceImpl"
participant Util as "JwtUtil"
participant DB as "CollabEntryMapper"
Admin->>Ctl : "POST /api/v1/collab (createEntry with permissions)"
Ctl->>Svc : "createEntry(entry with projectIds, pointIds, permissions)"
Svc->>Svc : "set token, status=1, validate permissions"
Svc->>DB : "save(entry with enhanced permissions)"
Svc-->>Ctl : "entry with token and permissions"
Ctl-->>Admin : "entry with comprehensive permissions"
Admin->>Ctl : "POST /api/v1/collab/{id}/issue-token"
Ctl->>Svc : "issueCollabToken(id)"
Svc->>Svc : "validate entry, TTL, and permission scope"
Svc->>Util : "generateCollabToken(entryId, entryName, ttl)"
Util-->>Svc : "JWT with enhanced claims"
Svc-->>Ctl : "JWT"
Ctl-->>Admin : "JWT"
Admin->>Ctl : "PUT /api/v1/collab/{id}/reset-token"
Ctl->>Svc : "resetToken(id)"
Svc->>Svc : "generate new token with updated permissions"
Svc->>DB : "update token"
Svc-->>Ctl : "new token"
Ctl-->>Admin : "new token"
```

**Diagram sources**
- [CollabEntryController.java:42-88](file://admin-backend/src/main/java/com/qhiot/survey/controller/CollabEntryController.java#L42-L88)
- [CollabEntryServiceImpl.java:44-141](file://admin-backend/src/main/java/com/qhiot/survey/service/impl/CollabEntryServiceImpl.java#L44-L141)
- [JwtUtil.java:57-68](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L57-L68)
- [CollabEntryMapper.java:1-9](file://admin-backend/src/main/java/com/qhiot/survey/mapper/CollabEntryMapper.java#L1-L9)

**Section sources**
- [CollabEntryServiceImpl.java:44-141](file://admin-backend/src/main/java/com/qhiot/survey/service/impl/CollabEntryServiceImpl.java#L44-L141)
- [CollabEntryController.java:42-88](file://admin-backend/src/main/java/com/qhiot/survey/controller/CollabEntryController.java#L42-L88)
- [JwtUtil.java:57-68](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L57-L68)

### Comprehensive Access Logging and Advanced Auditing
The access logging system now provides detailed audit trails with enhanced information capture:

- **Detailed logging**: Every access attempt is recorded in collab_access_log with entryId, token, client IP, user-agent, request path, response code, and timestamp.
- **Comprehensive audit trail**: Logs include permission validation results, object-level scope checks, and access policy decisions.
- **Enhanced persistence**: Logs are persisted even on failures (401/403), ensuring complete audit coverage for security monitoring.
- **Advanced querying**: Access logs support filtering by entryId, date ranges, response codes, and authorization outcomes for compliance reporting.

```mermaid
classDiagram
class CollabAccessLog {
+Long id
+Long entryId
+String token
+String ip
+String userAgent
+String requestPath
+Integer responseCode
+LocalDateTime createTime
}
class CollabAccessLogMapper {
+insert(record)
+selectList(wrapper)
+selectByEntryId(entryId)
+selectByDateRange(startDate, endDate)
}
class CollabSecurityService {
+logAccess(entryId, token, request, responseCode)
}
CollabSecurityService --> CollabAccessLog : "creates with enhanced details"
CollabSecurityService --> CollabAccessLogMapper : "persists comprehensive logs"
```

**Diagram sources**
- [CollabAccessLog.java:11-43](file://admin-backend/src/main/java/com/qhiot/survey/entity/CollabAccessLog.java#L11-L43)
- [CollabAccessLogMapper.java:1-12](file://admin-backend/src/main/java/com/qhiot/survey/mapper/CollabAccessLogMapper.java#L1-L12)
- [CollabSecurityService.java:257-271](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L257-L271)

**Section sources**
- [CollabSecurityService.java:257-271](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L257-L271)
- [CollabEntryServiceImpl.java:110-119](file://admin-backend/src/main/java/com/qhiot/survey/service/impl/CollabEntryServiceImpl.java#L110-L119)
- [01-init.sql:398-410](file://admin-backend/init-data/01-init.sql#L398-L410)

### Security Implications vs Internal Tokens
The enhanced collaboration token system provides significantly stronger security controls compared to internal tokens:

- **Internal tokens** (loginType=internal or missing): Follow standard authentication flow using UserDetailsService and provide full role/permission sets for administrative access.
- **Enhanced collaboration tokens**:
  - Scoped to specific collaboration entries with comprehensive validation against entry status, expiration, and permission scope.
  - Enforce strict white-list/black-list policies with object-level scope restrictions for projects and points.
  - Carry dedicated ROLE_COLLAB with limited access to read-only operations only.
  - Trigger comprehensive audit logs with detailed permission validation results.
  - Support granular permission definitions including project:view, point:view, template:view, and audit:view capabilities.
- **Enhanced endpoint restrictions**: Collaboration tokens are denied access to all administrative operations, exports, user/role/system management, dictionaries, health actuator endpoints, and any write operations.

**Section sources**
- [JwtAuthenticationFilter.java:26-34](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L26-L34)
- [CollabSecurityService.java:77-117](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L77-L117)
- [CollabTokenSecurityTest.java:126-144](file://admin-backend/src/test/java/com/qhiot/survey/security/CollabTokenSecurityTest.java#L126-L144)

### Examples and Enhanced Workflows

- **Example**: Generate an enhanced collaboration token with comprehensive permissions
  - Admin creates collaboration entry with projectIds="[100,101]", pointIds="[200,201]", and permissions='["project:view","point:view","audit:view"]'
  - Admin calls POST /api/v1/collab/{id}/issue-token to obtain JWT with loginType=collab and embedded collabEntryId
  - Token's TTL derived from entry's expireTime or defaults to 7 days with enhanced permission validation

- **Example**: Validate collaboration token with object-level scope enforcement
  - External platform sends Authorization: Bearer <JWT> to GET /api/v1/project/100
  - Filter validates token, checks entry validity, enforces white-list policy, validates project ID scope (100), sets ROLE_COLLAB, and proceeds

- **Example**: Attempt access to unauthorized scope
  - External platform tries GET /api/v1/project/999 with token authorized only for project 100
  - Filter denies access (403) due to object-level scope violation, logs the attempt with scope validation details

- **Example**: Write operation blocked despite whitelisted endpoint
  - External platform tries POST /api/v1/point with token authorized to GET only
  - Filter denies access (403) due to method restriction, logs the attempt with policy decision details

- **Example**: Entry revocation with immediate effect
  - Admin revokes collaboration entry (status=3)
  - Any subsequent access attempts with tokens tied to that entry return 401 with detailed revocation logging

- **Example**: Token reset with updated permissions
  - Admin resets token for entry with updated project scope
  - Old token becomes invalid immediately; new token reflects updated permissions for future use

**Section sources**
- [CollabEntryController.java:56-88](file://admin-backend/src/main/java/com/qhiot/survey/controller/CollabEntryController.java#L56-L88)
- [CollabEntryServiceImpl.java:66-89](file://admin-backend/src/main/java/com/qhiot/survey/service/impl/CollabEntryServiceImpl.java#L66-L89)
- [JwtAuthenticationFilter.java:88-130](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L88-L130)
- [CollabTokenSecurityTest.java:172-276](file://admin-backend/src/test/java/com/qhiot/survey/security/CollabTokenSecurityTest.java#L172-L276)

### Integration Patterns and Advanced Collaborative Workflow Management
The enhanced collaboration system supports sophisticated integration patterns with comprehensive security controls:

- **External platforms** receive collaboration JWTs with comprehensive permission scopes from the admin backend
- **Scoped access** enables platforms to access specific projects and points defined in the collaboration entry
- **Granular permissions** allow fine-tuned control over read operations including projects, points, results, templates, and audit data
- **Administrators** manage collaboration entries with detailed permission configurations, set expirations, revoke access, and reset tokens as needed
- **Advanced monitoring** through comprehensive access logs enables security monitoring, compliance reporting, and incident response by correlating entryId, IP, request paths, and authorization outcomes
- **Audit compliance** with detailed logging of permission validations, scope enforcement, and access decisions for regulatory requirements

**Section sources**
- [CollabEntryController.java:19-89](file://admin-backend/src/main/java/com/qhiot/survey/controller/CollabEntryController.java#L19-L89)
- [CollabEntryServiceImpl.java:110-119](file://admin-backend/src/main/java/com/qhiot/survey/service/impl/CollabEntryServiceImpl.java#L110-L119)
- [01-init.sql:212-229](file://admin-backend/init-data/01-init.sql#L212-L229)

## Dependency Analysis
The enhanced collaboration token system maintains low coupling with improved separation of concerns:
- JwtAuthenticationFilter depends on JwtUtil for token parsing and CollabSecurityService for comprehensive validation and access control.
- CollabSecurityService depends on CollabEntryMapper and CollabAccessLogMapper for enhanced persistence with detailed audit logging.
- CollabEntryServiceImpl orchestrates business logic, enhanced token generation with permission validation, and comprehensive access log retrieval.
- Controllers expose management APIs for collaboration entries with enhanced validation and token issuance.

```mermaid
graph LR
F["JwtAuthenticationFilter"] --> U["JwtUtil"]
F --> S["CollabSecurityService"]
S --> M1["CollabEntryMapper"]
S --> M2["CollabAccessLogMapper"]
S --> P["Permissions"]
CEC["CollabEntryController"] --> CES["CollabEntryService"]
CES --> CEI["CollabEntryServiceImpl"]
CEI --> M1
CEI --> M2
CEI --> U
```

**Diagram sources**
- [JwtAuthenticationFilter.java:41-43](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L41-L43)
- [CollabSecurityService.java:43-45](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L43-L45)
- [CollabEntryServiceImpl.java:28-32](file://admin-backend/src/main/java/com/qhiot/survey/service/impl/CollabEntryServiceImpl.java#L28-L32)
- [CollabEntryController.java:24-25](file://admin-backend/src/main/java/com/qhiot/survey/controller/CollabEntryController.java#L24-L25)

**Section sources**
- [JwtAuthenticationFilter.java:41-43](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L41-L43)
- [CollabSecurityService.java:43-45](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L43-L45)
- [CollabEntryServiceImpl.java:28-32](file://admin-backend/src/main/java/com/qhiot/survey/service/impl/CollabEntryServiceImpl.java#L28-L32)
- [CollabEntryController.java:24-25](file://admin-backend/src/main/java/com/qhiot/survey/controller/CollabEntryController.java#L24-L25)

## Performance Considerations
The enhanced collaboration token system maintains optimal performance with additional security features:
- Token validation remains lightweight relying on claim extraction and in-memory checks with enhanced permission validation; database reads occur only for entry lookup and comprehensive access log insertion.
- Access log writes are best-effort with enhanced error handling and wrapped in try/catch blocks to avoid impacting request processing latency.
- Enhanced indexing on collab_entry.token, collab_access_log entry_id, and collab_access_log authorization_scope improves query performance for lookups, audits, and scope validation.
- Permission validation occurs efficiently using JSON-based permission storage with optimized lookup algorithms.

**Section sources**
- [CollabSecurityService.java:257-271](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L257-L271)
- [01-init.sql:227-228](file://admin-backend/init-data/01-init.sql#L227-L228)
- [01-init.sql:408-409](file://admin-backend/init-data/01-init.sql#L408-L409)

## Troubleshooting Guide
Enhanced troubleshooting guidance for the improved collaboration token system:

- **401 Unauthorized on collaboration token**:
  - Indicates entry does not exist, is not enabled, has expired, or has been revoked. Verify entry status, expiration, and revocation status.
- **403 Forbidden on collaboration token**:
  - Multiple possible causes: requested endpoint not whitelisted, HTTP method not allowed, object-level scope violation, or permission validation failure. Check endpoint whitelist, method restrictions, and authorization scope.
- **No internal UserDetailsService invocation**:
  - Confirms token processed as collaboration token. Internal tokens would trigger UserDetailsService. Collaboration tokens bypass standard authentication flow.
- **Access logs not appearing**:
  - Check collab_access_log persistence, ensure no exceptions occurred during log insertion, verify enhanced logging configuration.
- **Permission validation errors**:
  - Verify collaboration entry contains proper JSON-encoded permissions array, check projectIds and pointIds arrays match expected formats, ensure permission strings follow required format.
- **Scope enforcement issues**:
  - Confirm object-level scope arrays (projectIds, pointIds) contain correct identifiers, verify parameter-based scope validation for list endpoints, check URI pattern matching for scope enforcement.

**Section sources**
- [JwtAuthenticationFilter.java:88-130](file://admin-backend/src/main/java/com/qhiot/survey/security/JwtAuthenticationFilter.java#L88-L130)
- [CollabSecurityService.java:257-271](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L257-L271)
- [CollabTokenSecurityTest.java:172-276](file://admin-backend/src/test/java/com/qhiot/survey/security/CollabTokenSecurityTest.java#L172-L276)

## Conclusion
The enhanced collaboration token system provides a robust, auditable mechanism for granting controlled access to specific survey resources across platforms. With comprehensive white-list based access control policies, object-level scope restrictions, and detailed audit logging, the system significantly strengthens security while enabling flexible integrations. The enhanced permission model allows fine-grained control over read operations, administrators retain full control over lifecycle events with comprehensive logging, and the advanced audit capabilities support ongoing monitoring, compliance, and security incident response.

## Appendices

### Database Schema Notes
- **collab_entry**: stores entry metadata, token, comprehensive permissions, project scope, point scope, and lifecycle fields with enhanced permission validation.
- **collab_access_log**: records detailed access attempts with authorization scope validation and permission decision logs for comprehensive auditing.

**Section sources**
- [01-init.sql:212-229](file://admin-backend/init-data/01-init.sql#L212-L229)
- [01-init.sql:398-410](file://admin-backend/init-data/01-init.sql#L398-L410)

### Runtime Configuration
- JWT secret and expiration configured via application.yml and injected into JwtUtil for enhanced token generation.
- Enhanced security policies configured through CollabSecurityService with comprehensive access control rules.

**Section sources**
- [application.yml:9-13](file://admin-backend/src/main/resources/application.yml#L9-L13)
- [JwtUtil.java:22-29](file://admin-backend/src/main/java/com/qhiot/survey/common/util/JwtUtil.java#L22-L29)
- [CollabSecurityService.java:38-45](file://admin-backend/src/main/java/com/qhiot/survey/security/CollabSecurityService.java#L38-L45)
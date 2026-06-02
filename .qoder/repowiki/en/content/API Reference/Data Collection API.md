# Data Collection API

<cite>
**Referenced Files in This Document**
- [SurveyResultController.java](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyResultController.java)
- [SurveyTemplateController.java](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyTemplateController.java)
- [FileUploadController.java](file://admin-backend/src/main/java/com/qhiot/survey/controller/FileUploadController.java)
- [OfflineDataSyncController.java](file://admin-backend/src/main/java/com/qhiot/survey/controller/OfflineDataSyncController.java)
- [SurveyResultService.java](file://admin-backend/src/main/java/com/qhiot/survey/service/SurveyResultService.java)
- [SurveyTemplateService.java](file://admin-backend/src/main/java/com/qhiot/survey/service/SurveyTemplateService.java)
- [FileUploadService.java](file://admin-backend/src/main/java/com/qhiot/survey/service/FileUploadService.java)
- [OfflineDataSyncService.java](file://admin-backend/src/main/java/com/qhiot/survey/service/OfflineDataSyncService.java)
- [SurveyResult.java](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyResult.java)
- [SurveyTemplate.java](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyTemplate.java)
- [SurveyTemplateVersion.java](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyTemplateVersion.java)
- [OfflineDataSync.java](file://admin-backend/src/main/java/com/qhiot/survey/entity/OfflineDataSync.java)
- [survey.vue](file://mobile-app/src/pages/survey/survey.vue)
- [dynamic-form.vue](file://mobile-app/src/components/dynamic-form/dynamic-form.vue)
- [api.js](file://mobile-app/src/utils/api.js)
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
This document provides comprehensive API documentation for data collection and form submission endpoints in the Survey App. It covers:
- Survey result creation, updates, auditing, and versioning
- Dynamic form template system and how survey results map to template structures
- Image and file uploads, including watermarks and fallback storage
- Offline data synchronization, conflict resolution, and retry mechanisms
- Validation rules, data transformation, and audit trail generation
- End-to-end survey workflows from initiation to completion

## Project Structure
The backend exposes REST APIs under /api/v1 grouped by domain:
- /result: survey result CRUD, submission, auditing, and diffs
- /template: template lifecycle, versions, bindings, and previews
- /file: single and batch file uploads, deletions
- /offline-sync: offline data ingestion, pending records, sync status, conflict resolution, retries, cleanup

```mermaid
graph TB
subgraph "Mobile App"
MVUE["survey.vue"]
DYN["dynamic-form.vue"]
APIJS["api.js"]
end
subgraph "Backend Controllers"
RC["SurveyResultController"]
TC["SurveyTemplateController"]
FC["FileUploadController"]
OSC["OfflineDataSyncController"]
end
subgraph "Services"
RS["SurveyResultService"]
TS["SurveyTemplateService"]
FS["FileUploadService"]
OSS["OfflineDataSyncService"]
end
subgraph "Entities"
SR["SurveyResult"]
ST["SurveyTemplate"]
STV["SurveyTemplateVersion"]
ODS["OfflineDataSync"]
end
MVUE --> DYN
MVUE --> APIJS
APIJS --> RC
APIJS --> TC
APIJS --> FC
APIJS --> OSC
RC --> RS
TC --> TS
FC --> FS
OSC --> OSS
RS --> SR
TS --> ST
TS --> STV
OSS --> ODS
```

**Diagram sources**
- [SurveyResultController.java:24-181](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyResultController.java#L24-L181)
- [SurveyTemplateController.java:27-194](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyTemplateController.java#L27-L194)
- [FileUploadController.java:17-80](file://admin-backend/src/main/java/com/qhiot/survey/controller/FileUploadController.java#L17-L80)
- [OfflineDataSyncController.java:18-95](file://admin-backend/src/main/java/com/qhiot/survey/controller/OfflineDataSyncController.java#L18-L95)
- [SurveyResultService.java:11-81](file://admin-backend/src/main/java/com/qhiot/survey/service/SurveyResultService.java#L11-L81)
- [SurveyTemplateService.java:12-59](file://admin-backend/src/main/java/com/qhiot/survey/service/SurveyTemplateService.java#L12-L59)
- [FileUploadService.java:20-122](file://admin-backend/src/main/java/com/qhiot/survey/service/FileUploadService.java#L20-L122)
- [OfflineDataSyncService.java:12-84](file://admin-backend/src/main/java/com/qhiot/survey/service/OfflineDataSyncService.java#L12-L84)
- [SurveyResult.java:14-93](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyResult.java#L14-L93)
- [SurveyTemplate.java:13-61](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyTemplate.java#L13-L61)
- [SurveyTemplateVersion.java:13-38](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyTemplateVersion.java#L13-L38)
- [OfflineDataSync.java:15-97](file://admin-backend/src/main/java/com/qhiot/survey/entity/OfflineDataSync.java#L15-L97)

**Section sources**
- [SurveyResultController.java:24-181](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyResultController.java#L24-L181)
- [SurveyTemplateController.java:27-194](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyTemplateController.java#L27-L194)
- [FileUploadController.java:17-80](file://admin-backend/src/main/java/com/qhiot/survey/controller/FileUploadController.java#L17-L80)
- [OfflineDataSyncController.java:18-95](file://admin-backend/src/main/java/com/qhiot/survey/controller/OfflineDataSyncController.java#L18-L95)

## Core Components
- SurveyResultController: CRUD, submission, auditing, drafts, and version diff
- SurveyTemplateController: template lifecycle, publishing versions, field configs, bindings
- FileUploadController: single/multiple file upload, deletion
- OfflineDataSyncController: batch receive, pending queries, sync, conflict resolution, retry, cleanup

Key responsibilities:
- Validate roles via @PreAuthorize
- Enforce optimistic locking on updates and submissions
- Support audit trails and version diffs
- Provide offline-first ingestion with conflict handling

**Section sources**
- [SurveyResultController.java:24-181](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyResultController.java#L24-L181)
- [SurveyTemplateController.java:27-194](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyTemplateController.java#L27-L194)
- [FileUploadController.java:17-80](file://admin-backend/src/main/java/com/qhiot/survey/controller/FileUploadController.java#L17-L80)
- [OfflineDataSyncController.java:18-95](file://admin-backend/src/main/java/com/qhiot/survey/controller/OfflineDataSyncController.java#L18-L95)

## Architecture Overview
The system follows a layered architecture:
- Presentation: Spring MVC controllers expose REST endpoints
- Application: Services encapsulate business logic
- Persistence: Entities mapped to database tables via MyBatis-Plus
- Storage: FileUploadService supports OSS and local fallback
- Offline: OfflineDataSync tracks device ingestion and sync state

```mermaid
sequenceDiagram
participant Mobile as "Mobile App"
participant API as "Controllers"
participant Svc as "Services"
participant DB as "Database"
participant Store as "OSS/Local"
Mobile->>API : Submit form data
API->>Svc : createResult/saveDraft/submitForAudit
Svc->>DB : Persist SurveyResult
Mobile->>API : Upload images/documents
API->>Svc : uploadFile
Svc->>Store : Save file
Store-->>Svc : File URL
Svc-->>API : Upload result
API-->>Mobile : Result payload
```

**Diagram sources**
- [SurveyResultController.java:59-153](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyResultController.java#L59-L153)
- [FileUploadController.java:25-43](file://admin-backend/src/main/java/com/qhiot/survey/controller/FileUploadController.java#L25-L43)
- [FileUploadService.java:36-96](file://admin-backend/src/main/java/com/qhiot/survey/service/FileUploadService.java#L36-L96)
- [SurveyResult.java:19-42](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyResult.java#L19-L42)

## Detailed Component Analysis

### Survey Result Management
Endpoints:
- GET /result/list, /result/{id}, /result/point/{pointId}/latest
- POST /result/create, PUT /result/update/{id}, DELETE /result/delete/{id}
- GET /result/audit/page, POST /result/audit/{id}/pass, POST /result/audit/{id}/reject, POST /result/audit/batch-pass
- POST /result/submit/{id} (with optional expectedVersion)
- POST /result/draft
- GET /result/version/diff?currentId&compareId
- GET /result/user/{surveyUserId}

Processing logic:
- Role gating via @PreAuthorize for COLLECTOR, AUDITOR, ADMIN
- Optimistic locking enforced on update and submit endpoints
- Submission transitions result into audit queue
- Drafts saved per point and user
- Version diffs support comparison across versions

```mermaid
sequenceDiagram
participant Mobile as "Mobile App"
participant RC as "SurveyResultController"
participant RS as "SurveyResultService"
participant DB as "Database"
Mobile->>RC : POST /result/draft
RC->>RS : saveDraft(result, userId)
RS->>DB : INSERT/UPDATE SurveyResult
DB-->>RS : Saved result
RS-->>RC : Saved result
RC-->>Mobile : Saved result
Mobile->>RC : POST /result/submit/{id}?versionNo=...
RC->>RS : submitForAudit(id, userId, versionNo)
RS->>DB : UPDATE status + audit_status (optimistic lock)
DB-->>RS : Updated row
RS-->>RC : Success
RC-->>Mobile : Success
```

**Diagram sources**
- [SurveyResultController.java:134-153](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyResultController.java#L134-L153)
- [SurveyResultService.java:64-70](file://admin-backend/src/main/java/com/qhiot/survey/service/SurveyResultService.java#L64-L70)
- [SurveyResult.java:45-52](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyResult.java#L45-L52)

**Section sources**
- [SurveyResultController.java:33-181](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyResultController.java#L33-L181)
- [SurveyResultService.java:11-81](file://admin-backend/src/main/java/com/qhiot/survey/service/SurveyResultService.java#L11-L81)
- [SurveyResult.java:19-93](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyResult.java#L19-L93)

### Dynamic Form Template System
Endpoints:
- GET /template/page, /template/list, /template/detail/{id}
- POST /template/create, PUT /template/update/{id}, DELETE /template/delete/{id}
- PUT /template/draft/{id}, POST /{id}/publish
- GET /{id}/preview, GET /{id}/versions, GET /version/{versionId}, GET /version/{versionId}/fields
- POST /template/bind-outfall, GET /template/binding, GET /template/bindings, DELETE /template/binding/{bindingId}

Template mapping:
- Templates are bound to project/section/outfallType via SurveyPointTemplateBinding
- Mobile loads binding, fetches fields JSON, renders dynamic form
- Fields include validation rules, linkage rules, and option sources

```mermaid
sequenceDiagram
participant Mobile as "Mobile App"
participant TC as "SurveyTemplateController"
participant TS as "SurveyTemplateService"
participant DB as "Database"
Mobile->>TC : GET /template/binding?projectId&sectionId&outfallType
TC->>TS : getBindingByOutfallType(...)
TS->>DB : SELECT binding
DB-->>TS : Binding
TS-->>TC : Binding
TC-->>Mobile : Binding {templateVersionId,...}
Mobile->>TC : GET /template/version/{versionId}/fields
TC->>TS : getFieldConfig(versionId)
TS->>DB : SELECT fieldsJson/rulesJson/linkageRulesJson
DB-->>TS : Config
TS-->>TC : Config
TC-->>Mobile : fieldsJson
```

**Diagram sources**
- [SurveyTemplateController.java:157-178](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyTemplateController.java#L157-L178)
- [SurveyTemplateController.java:151-155](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyTemplateController.java#L151-L155)
- [SurveyTemplateService.java:43-51](file://admin-backend/src/main/java/com/qhiot/survey/service/SurveyTemplateService.java#L43-L51)
- [SurveyTemplate.java:18-51](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyTemplate.java#L18-L51)
- [SurveyTemplateVersion.java:18-30](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyTemplateVersion.java#L18-L30)

**Section sources**
- [SurveyTemplateController.java:38-194](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyTemplateController.java#L38-L194)
- [SurveyTemplateService.java:12-59](file://admin-backend/src/main/java/com/qhiot/survey/service/SurveyTemplateService.java#L12-L59)
- [SurveyTemplate.java:18-61](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyTemplate.java#L18-L61)
- [SurveyTemplateVersion.java:18-38](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyTemplateVersion.java#L18-L38)

### File Upload Endpoints
Endpoints:
- POST /file/upload (single)
- POST /file/upload/multiple (batch)
- DELETE /file/delete?fileUrl

Behavior:
- Watermarking for images with collector name and coordinates
- Fallback to local storage if OSS unavailable
- Returns filename and URL for successful uploads

```mermaid
flowchart TD
Start(["Upload Request"]) --> CheckOSS["OSS client available?"]
CheckOSS --> |Yes| BuildKey["Build OSS key<br/>folder/filename"]
BuildKey --> PutObject["putObject(bucket, key, stream)"]
PutObject --> ReturnURL["Return https://bucket.oss-region/key"]
CheckOSS --> |No| LocalPath["Ensure local uploads dir exists"]
LocalPath --> CopyFile["Copy stream to ./uploads/filename"]
CopyFile --> ReturnLocal["Return /api/files/filename"]
```

**Diagram sources**
- [FileUploadController.java:25-43](file://admin-backend/src/main/java/com/qhiot/survey/controller/FileUploadController.java#L25-L43)
- [FileUploadService.java:36-96](file://admin-backend/src/main/java/com/qhiot/survey/service/FileUploadService.java#L36-L96)

**Section sources**
- [FileUploadController.java:25-80](file://admin-backend/src/main/java/com/qhiot/survey/controller/FileUploadController.java#L25-L80)
- [FileUploadService.java:20-122](file://admin-backend/src/main/java/com/qhiot/survey/service/FileUploadService.java#L20-L122)

### Offline Data Synchronization
Endpoints:
- POST /offline-sync/receive (requires Device-Id header)
- GET /offline-sync/pending?pageNum&pageSize
- POST /offline-sync/sync/{syncId}, POST /offline-sync/sync/batch
- GET /offline-sync/status
- POST /offline-sync/conflict/{syncId}?resolution&mergedData
- POST /offline-sync/retry/{syncId}
- POST /offline-sync/cleanup?days=30

Conflict resolution strategies:
- server: adopt server-side data
- client: adopt client-side data
- merge: apply mergedData payload

```mermaid
sequenceDiagram
participant Mobile as "Mobile App"
participant OSC as "OfflineDataSyncController"
participant OSS as "OfflineDataSyncService"
participant DB as "Database"
Mobile->>OSC : POST /offline-sync/receive (Device-Id, userId, dataList)
OSC->>OSS : receiveOfflineData(deviceId, userId, dataList)
OSS->>DB : INSERT offline_data_sync records (status=0)
DB-->>OSS : Inserted
OSS-->>OSC : {acceptedCount, pendingCount}
OSC-->>Mobile : Result
Mobile->>OSC : GET /offline-sync/pending?pageNum&pageSize
OSC->>OSS : getPendingSyncData(deviceId, pageNum, pageSize)
OSS->>DB : SELECT pending records
DB-->>OSS : Page<OfflineDataSync>
OSS-->>OSC : Page
OSC-->>Mobile : Page
Mobile->>OSC : POST /offline-sync/conflict/{syncId}?resolution=merge&mergedData=...
OSC->>OSS : resolveConflict(syncId, resolution, mergedData)
OSS->>DB : UPDATE conflictResolution + dataContent
DB-->>OSS : Updated
OSS-->>OSC : Success
OSC-->>Mobile : Success
```

**Diagram sources**
- [OfflineDataSyncController.java:26-78](file://admin-backend/src/main/java/com/qhiot/survey/controller/OfflineDataSyncController.java#L26-L78)
- [OfflineDataSyncService.java:14-66](file://admin-backend/src/main/java/com/qhiot/survey/service/OfflineDataSyncService.java#L14-L66)
- [OfflineDataSync.java:23-91](file://admin-backend/src/main/java/com/qhiot/survey/entity/OfflineDataSync.java#L23-L91)

**Section sources**
- [OfflineDataSyncController.java:26-95](file://admin-backend/src/main/java/com/qhiot/survey/controller/OfflineDataSyncController.java#L26-L95)
- [OfflineDataSyncService.java:12-84](file://admin-backend/src/main/java/com/qhiot/survey/service/OfflineDataSyncService.java#L12-L84)
- [OfflineDataSync.java:15-97](file://admin-backend/src/main/java/com/qhiot/survey/entity/OfflineDataSync.java#L15-L97)

### Mobile App Integration
- survey.vue orchestrates template loading, dictionary data, draft persistence, and submission
- dynamic-form.vue renders fields, validates, auto-fills locations, and handles linkage rules
- api.js centralizes HTTP requests, interceptors, and unified response handling

```mermaid
sequenceDiagram
participant MVUE as "survey.vue"
participant DYN as "dynamic-form.vue"
participant APIJS as "api.js"
participant RC as "SurveyResultController"
participant TC as "SurveyTemplateController"
participant FC as "FileUploadController"
MVUE->>TC : GET binding + version fields
TC-->>MVUE : fieldsJson
MVUE->>DYN : Render form with fields + dict
DYN-->>MVUE : getFormData()
MVUE->>APIJS : resultApi.saveDraft({pointId, formData})
APIJS->>RC : POST /result/draft
MVUE->>APIJS : resultApi.submit({id, formData})
APIJS->>RC : POST /result/submit/{id}
MVUE->>APIJS : fileApi.uploadMultiple(paths)
APIJS->>FC : POST /file/upload/multiple
```

**Diagram sources**
- [survey.vue:69-141](file://mobile-app/src/pages/survey/survey.vue#L69-L141)
- [dynamic-form.vue:16-306](file://mobile-app/src/components/dynamic-form/dynamic-form.vue#L16-L306)
- [api.js:264-360](file://mobile-app/src/utils/api.js#L264-L360)

**Section sources**
- [survey.vue:32-159](file://mobile-app/src/pages/survey/survey.vue#L32-L159)
- [dynamic-form.vue:146-336](file://mobile-app/src/components/dynamic-form/dynamic-form.vue#L146-L336)
- [api.js:1-370](file://mobile-app/src/utils/api.js#L1-L370)

## Dependency Analysis
- Controllers depend on Services for business logic
- Services operate on Entities persisted via MyBatis-Plus
- FileUploadService depends on OSS client and local filesystem
- OfflineDataSyncService manages ingestion and conflict resolution state

```mermaid
graph LR
RC["SurveyResultController"] --> RS["SurveyResultService"]
TC["SurveyTemplateController"] --> TS["SurveyTemplateService"]
FC["FileUploadController"] --> FS["FileUploadService"]
OSC["OfflineDataSyncController"] --> OSS["OfflineDataSyncService"]
RS --> SR["SurveyResult"]
TS --> ST["SurveyTemplate"]
TS --> STV["SurveyTemplateVersion"]
OSS --> ODS["OfflineDataSync"]
```

**Diagram sources**
- [SurveyResultController.java:24-181](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyResultController.java#L24-L181)
- [SurveyTemplateController.java:27-194](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyTemplateController.java#L27-L194)
- [FileUploadController.java:17-80](file://admin-backend/src/main/java/com/qhiot/survey/controller/FileUploadController.java#L17-L80)
- [OfflineDataSyncController.java:18-95](file://admin-backend/src/main/java/com/qhiot/survey/controller/OfflineDataSyncController.java#L18-L95)
- [SurveyResultService.java:11-81](file://admin-backend/src/main/java/com/qhiot/survey/service/SurveyResultService.java#L11-L81)
- [SurveyTemplateService.java:12-59](file://admin-backend/src/main/java/com/qhiot/survey/service/SurveyTemplateService.java#L12-L59)
- [FileUploadService.java:20-122](file://admin-backend/src/main/java/com/qhiot/survey/service/FileUploadService.java#L20-L122)
- [OfflineDataSyncService.java:12-84](file://admin-backend/src/main/java/com/qhiot/survey/service/OfflineDataSyncService.java#L12-L84)
- [SurveyResult.java:14-93](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyResult.java#L14-L93)
- [SurveyTemplate.java:13-61](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyTemplate.java#L13-L61)
- [SurveyTemplateVersion.java:13-38](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyTemplateVersion.java#L13-L38)
- [OfflineDataSync.java:15-97](file://admin-backend/src/main/java/com/qhiot/survey/entity/OfflineDataSync.java#L15-L97)

**Section sources**
- [SurveyResultController.java:24-181](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyResultController.java#L24-L181)
- [SurveyTemplateController.java:27-194](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyTemplateController.java#L27-L194)
- [FileUploadController.java:17-80](file://admin-backend/src/main/java/com/qhiot/survey/controller/FileUploadController.java#L17-L80)
- [OfflineDataSyncController.java:18-95](file://admin-backend/src/main/java/com/qhiot/survey/controller/OfflineDataSyncController.java#L18-L95)

## Performance Considerations
- Template caching: Published template version retrieval is frequently called; cache eviction is supported to avoid stale configurations.
- Batch operations: Use batch endpoints for multiple files and offline sync to reduce overhead.
- Watermarking cost: Image watermarking adds CPU overhead; consider async processing for large batches.
- Pagination: Audit and offline sync endpoints support pagination to limit payload sizes.
- Indexes: Ensure database indexes on frequently queried fields (e.g., pointId, templateVersionId, deviceId).

## Troubleshooting Guide
Common issues and resolutions:
- Authentication failures (401): Ensure Authorization header is present and valid; the interceptor removes expired tokens and redirects to login.
- Authorization failures (403): Verify user roles (COLLECTOR, AUDITOR, ADMIN) for protected endpoints.
- Optimistic lock conflicts (409): When submitting or updating, pass the latest expectedVersion to avoid conflicts.
- Upload failures: Confirm OSS availability; the service falls back to local storage and returns a local URL.
- Offline sync stuck: Check pending records, retry failed entries, and resolve conflicts using server/client/merge strategies.

**Section sources**
- [api.js:40-71](file://mobile-app/src/utils/api.js#L40-L71)
- [SurveyResultController.java:134-144](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyResultController.java#L134-L144)
- [FileUploadService.java:78-96](file://admin-backend/src/main/java/com/qhiot/survey/service/FileUploadService.java#L78-L96)
- [OfflineDataSyncController.java:68-85](file://admin-backend/src/main/java/com/qhiot/survey/controller/OfflineDataSyncController.java#L68-L85)

## Conclusion
The Data Collection API provides a robust foundation for survey data capture, templating, file handling, and offline-first workflows. By leveraging dynamic forms, strict validation, audit trails, and conflict resolution, the system supports reliable data collection across diverse environments.

## Appendices

### API Definitions

- Survey Results
  - POST /api/v1/result/draft
    - Body: SurveyResult
    - Description: Save a draft for a point
  - POST /api/v1/result/submit/{id}?versionNo={n}
    - Body: none
    - Description: Submit result for audit with optimistic lock
  - POST /api/v1/result/create
    - Body: SurveyResult
    - Description: Create a new result
  - PUT /api/v1/result/update/{id}?expectedVersion={n}
    - Body: SurveyResult
    - Description: Update result with optimistic lock
  - GET /api/v1/result/version/diff?currentId&compareId
    - Description: Compare two versions
  - GET /api/v1/result/audit/page?projectId&sectionId&status&pageNum&pageSize
    - Description: Paginated audit list

- Templates
  - GET /api/v1/template/binding?projectId&sectionId&outfallType
    - Description: Get binding for outfall type
  - GET /api/v1/template/version/{versionId}/fields
    - Description: Get field configuration JSON
  - POST /api/v1/template/{id}/publish
    - Body: { fields, rules, linkageRules }
    - Description: Publish template and create new version

- Files
  - POST /api/v1/file/upload
    - Form-Data: file
    - Description: Upload single file
  - POST /api/v1/file/upload/multiple
    - Form-Data: files[]
    - Description: Upload multiple files
  - DELETE /api/v1/file/delete?fileUrl
    - Description: Delete file by URL

- Offline Sync
  - POST /api/v1/offline-sync/receive
    - Headers: Device-Id
    - Body: [{...}]
    - Description: Receive batch offline data
  - GET /api/v1/offline-sync/pending?pageNum&pageSize
    - Description: Pending records
  - POST /api/v1/offline-sync/conflict/{syncId}?resolution&mergedData
    - Description: Resolve conflict (server/client/merge)
  - POST /api/v1/offline-sync/retry/{syncId}
    - Description: Retry failed sync
  - POST /api/v1/offline-sync/cleanup?days
    - Description: Cleanup expired records

**Section sources**
- [SurveyResultController.java:59-161](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyResultController.java#L59-L161)
- [SurveyTemplateController.java:157-178](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyTemplateController.java#L157-L178)
- [SurveyTemplateController.java:151-155](file://admin-backend/src/main/java/com/qhiot/survey/controller/SurveyTemplateController.java#L151-L155)
- [FileUploadController.java:25-79](file://admin-backend/src/main/java/com/qhiot/survey/controller/FileUploadController.java#L25-L79)
- [OfflineDataSyncController.java:26-93](file://admin-backend/src/main/java/com/qhiot/survey/controller/OfflineDataSyncController.java#L26-L93)

### Data Models

```mermaid
erDiagram
SURVEY_RESULT {
bigint id PK
bigint point_id
int version_no
bigint template_version_id
json form_data
json images
int result_status
int audit_status
varchar audit_remark
bigint survey_user_id
int optimistic_lock_version
datetime submit_time
datetime audit_time
bigint auditor_id
int is_deleted
datetime create_time
datetime update_time
}
SURVEY_TEMPLATE {
bigint id PK
varchar template_name
varchar template_code
varchar description
int status
varchar fields_json
bigint current_version_id
varchar outlet_type
bigint creator_id
datetime create_time
datetime update_time
}
SURVEY_TEMPLATE_VERSION {
bigint id PK
bigint template_id
int version_no
json fields_json
json rules_json
json linkage_rules_json
int status
datetime publish_time
bigint creator_id
datetime create_time
}
OFFLINE_DATA_SYNC {
bigint id PK
varchar device_id
bigint user_id
varchar data_type
varchar data_id
json data_content
int sync_status
int retry_count
int max_retry_count
varchar error_message
datetime client_create_time
datetime server_receive_time
datetime sync_complete_time
int version_no
varchar conflict_resolution
datetime create_time
datetime update_time
}
SURVEY_RESULT ||--o{ SURVEY_TEMPLATE_VERSION : "uses"
SURVEY_TEMPLATE ||--o{ SURVEY_TEMPLATE_VERSION : "contains"
```

**Diagram sources**
- [SurveyResult.java:19-93](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyResult.java#L19-L93)
- [SurveyTemplate.java:18-61](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyTemplate.java#L18-L61)
- [SurveyTemplateVersion.java:18-38](file://admin-backend/src/main/java/com/qhiot/survey/entity/SurveyTemplateVersion.java#L18-L38)
- [OfflineDataSync.java:23-96](file://admin-backend/src/main/java/com/qhiot/survey/entity/OfflineDataSync.java#L23-L96)
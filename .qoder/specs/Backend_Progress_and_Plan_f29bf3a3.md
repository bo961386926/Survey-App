# Survey App Backend - Progress Report & Development Plan

---

## Part 1: Project Progress Report

### Overall Status: ~85% Feature Complete

| Module | Status | Notes |
|--------|--------|-------|
| Architecture & Infrastructure | DONE | Spring Boot 3.2.5, MyBatis-Plus, MySQL 8.0, Redis 7, Docker deployment |
| Auth & Security | DONE | JWT login, SMS verification, account lockout, role-based access (5 roles) |
| Project Management | DONE | CRUD, status workflow (draft->in_progress->paused->completed->archived), statistics |
| Survey Point Management | DONE | CRUD, geolocation, status tracking, assignment |
| Template Engine | DONE | Versioning, JSON field/rule/linkage configs, publish workflow, binding to outfall types |
| Data Submission & Audit | DONE | Submit, version tracking, pass/reject with remarks, audit trail |
| User Management | DONE | Multi-role, bulk import/export (Excel), password reset, enable/disable |
| Operation & Login Logging | DONE | AOP aspect-based, risk levels, IP/user-agent tracking |
| Dictionary System | DONE | Full CRUD for data dictionaries |
| File Upload | DONE | Aliyun OSS + local fallback, metadata tracking |
| Export Task System | DONE | Async export creation, status polling, download |
| Health & Monitoring | DONE | Health endpoint, readiness checks |
| Message Center | DONE | Controller exists, notification management |
| Collaboration Entry | DONE | Third-party token management controller exists |
| Location Correction | DONE | Correction logging endpoint |
| Offline Data Sync | PARTIAL | Framework & queue complete; 3 core sync methods have TODO placeholders |
| Test Coverage | WEAK | Only 11 test files for 24 controllers and 24+ services |
| Password Delivery | MISSING | Email/SMS notification for new passwords not implemented |

### Database: 23 Tables Implemented

- **Core Business:** project, project_section, project_member, survey_point, survey_result, survey_template, survey_template_version, survey_point_template_binding, survey_audit_record
- **Offline:** offline_data_sync
- **System:** sys_user, sys_role, sys_user_role, sys_permission, sys_role_permission, sys_dict, sys_dict_item, sys_config
- **Operations:** operation_log, login_log, location_correction_log, message_center, export_task, collab_entry, collab_access_log, sys_file

### API Surface: 24 Controllers

All major REST endpoints exist: `/api/v1/auth`, `/api/v1/project`, `/api/v1/point`, `/api/v1/template`, `/api/v1/survey-result`, `/api/v1/audit`, `/api/v1/export`, `/api/v1/offline-sync`, `/api/v1/messages`, `/api/v1/collab-entry`, `/api/v1/task`, `/api/v1/statistics`, etc.

### Known Issues

1. `OfflineDataSyncServiceImpl.java` - 3 TODO methods (survey_result sync, photo sync, location sync)
2. `SysUserServiceImpl.java` - Password delivery mechanism not implemented (line 337 comment)
3. Frontend legacy API paths partially mismatched with current RESTful backend convention
4. Only 11 test files - insufficient for production confidence

---

## Part 2: Backend Development Plan

### Task 1 (P0 - Critical): Complete Offline Data Sync

**File:** `admin-backend/src/main/java/com/qhiot/survey/service/impl/OfflineDataSyncServiceImpl.java`

**Scope:**
- Implement `survey_result` type sync logic (currently TODO at ~line 340)
- Implement photo data sync logic (TODO at ~line 351)
- Implement location data sync logic (TODO at ~line 359)
- Add conflict resolution for each type (server-wins/client-wins/merge strategies)
- Handle version_no validation during sync to prevent stale data overwrites

**Acceptance:** Mobile app can submit offline-collected data that correctly syncs to database with proper conflict handling.

---

### Task 2 (P0 - Critical): Verify Version Conflict Detection

**Scope:**
- Verify that `SurveyResultController` submit endpoint checks `version_no` before saving
- Ensure rejection flow creates a new draft with `version_no + 1` and marks original as read-only
- Verify `survey_audit_record` preserves rejection reason across versions
- Add missing logic if not fully implemented

**Acceptance:** Two clients submitting the same point - second submission rejected with version conflict error; reject-resubmit creates proper new version.

---

### Task 3 (P1 - Important): Implement Password Delivery

**File:** `admin-backend/src/main/java/com/qhiot/survey/service/impl/SysUserServiceImpl.java`

**Scope:**
- Implement password notification via SMS (leveraging existing Aliyun SMS integration)
- Add email fallback option using Spring Mail
- Secure the reset flow with rate limiting

**Acceptance:** When admin resets a user's password, the new password is delivered via SMS or email.

---

### Task 4 (P1 - Important): Expand Unit & Integration Tests

**Scope:**
- Add tests for authentication flow (login, token refresh, lockout)
- Add tests for survey result submission & version conflict
- Add tests for audit pass/reject workflow
- Add tests for offline sync logic (after Task 1)
- Add tests for export task lifecycle
- Target: Cover all P0 critical paths with automated tests

**Acceptance:** `mvn test` passes with coverage on all critical business flows.

---

### Task 5 (P1 - Important): Verify Frontend-Backend API Alignment

**Scope:**
- Confirm PC frontend (`admin-web-soybean/src/api/`) endpoint paths match actual backend controllers
- Confirm mobile app (`mobile-app/src/utils/api.js`) endpoint paths are compatible
- Fix any mismatches (either update frontend API files or add backend route aliases)
- Verify response format consistency: `{ code, message, data, requestId }`

**Acceptance:** All frontend API calls match actual backend endpoints; no 404s during integration.

---

### Task 6 (P1 - Important): Verify Export & PDF Generation

**Scope:**
- Verify `ExportTaskController` async job processing works end-to-end
- Confirm PDF generation via PDFBox/iText produces valid output
- Verify Excel export with Apache POI
- Test file download endpoint returns correct content

**Acceptance:** Creating an export task produces downloadable PDF/Excel files with correct survey data.

---

### Task 7 (P2 - Enhancement): Database Performance Optimization

**Scope:**
- Add indexes on frequently queried columns (project_id, status, created_at, user_id)
- Implement Redis caching for dictionary data, template versions, and user sessions
- Review MyBatis-Plus query efficiency for paginated list endpoints
- Reference existing file: `admin-backend/add-database-indexes.sql`

**Acceptance:** List/page endpoints respond within 200ms for datasets up to 10,000 records.

---

### Task 8 (P2 - Enhancement): Verify Collaboration Token Security

**Scope:**
- Verify `CollabEntryController` token generation and scope restrictions
- Confirm third-party tokens cannot access audit, delete, or full export endpoints
- Verify `collab_access_log` records all external access attempts
- Add integration tests for permission boundary enforcement

**Acceptance:** Third-party collaboration tokens are properly sandboxed; unauthorized access returns 403.

---

## Execution Order

```
Task 1 (Offline Sync) ──┐
Task 2 (Version Conflict) ──┼── Can run in parallel (independent modules)
Task 3 (Password Delivery) ──┘
         │
         ▼
Task 4 (Tests) ── Depends on Tasks 1-3 being complete
Task 5 (API Alignment) ── Can start immediately (independent)
Task 6 (Export Verify) ── Can start immediately (independent)
         │
         ▼
Task 7 (Performance) ── After core features verified
Task 8 (Collab Security) ── After core features verified
```

### Estimated Effort

| Task | Effort | Priority |
|------|--------|----------|
| Task 1: Offline Sync | ~4-6 hrs | P0 |
| Task 2: Version Conflict | ~2-3 hrs | P0 |
| Task 3: Password Delivery | ~2 hrs | P1 |
| Task 4: Test Expansion | ~6-8 hrs | P1 |
| Task 5: API Alignment | ~2-3 hrs | P1 |
| Task 6: Export Verify | ~2-3 hrs | P1 |
| Task 7: Performance | ~3-4 hrs | P2 |
| Task 8: Collab Security | ~2-3 hrs | P2 |

**Total estimated: ~23-32 hours of development work**

# Backend Connection Fix - ECONNREFUSED Error

## ❌ Problem

```
[vite] http proxy error: /api/auth/captcha
AggregateError [ECONNREFUSED]: 
    at internalConnectMultiple (node:net:1122:18)
    at afterConnectMultiple (node:net:1689:7)
```

## 🔍 Root Cause

**Port Mismatch:**
- **Frontend configured to connect to:** `http://localhost:8080`
- **Docker Compose exposes backend on:** `http://localhost:8081`
- **Docker internal port:** `8080` (mapped to host port `8081`)

From `docker-compose.yml` line 65:
```yaml
ports:
  - "8081:8080"  # Host port 8081 → Container port 8080
```

## ✅ Solution Applied

Updated `admin-web-soybean/.env`:
```diff
- VITE_SERVICE_BASE_URL=http://localhost:8080
+ VITE_SERVICE_BASE_URL=http://localhost:8081
```

## 🚀 Next Steps

### 1. Restart Frontend Development Server

The frontend needs to be restarted to pick up the new environment variable:

```bash
# Stop the current dev server (Ctrl+C)
# Then restart:
cd admin-web-soybean
pnpm dev
```

### 2. Verify Backend is Running

```bash
docker compose ps
```

Expected output:
```
NAME                   STATUS
survey-admin-backend   Up (healthy)
survey-mysql           Up (healthy)
survey-redis           Up (healthy)
```

### 3. Test Backend Health Check

```bash
curl http://localhost:8081/api/v1/health
```

Expected response:
```json
{
  "code": 200,
  "data": {
    "status": "UP",
    "timestamp": "2026-05-05T17:14:00"
  }
}
```

### 4. Test Captcha Endpoint

```bash
curl http://localhost:8081/api/auth/captcha
```

Expected response:
```json
{
  "code": 200,
  "data": {
    "key": "captcha:uuid-xxx",
    "image": "data:image/png;base64,..."
  }
}
```

## 🔧 Alternative Solutions

### Option A: Change Docker Port Mapping (if you prefer port 8080)

Modify `docker-compose.yml`:
```yaml
ports:
  - "8080:8080"  # Change from 8081 to 8080
```

Then restart Docker:
```bash
docker compose down
docker compose up -d
```

### Option B: Run Backend Locally (without Docker)

If you want to run the backend directly on your machine:

```bash
cd admin-backend
./apache-maven-3.9.6/bin/mvn spring-boot:run
```

This will start the backend on `http://localhost:8080` (default Spring Boot port).

Then keep the original `.env` configuration:
```env
VITE_SERVICE_BASE_URL=http://localhost:8080
```

## 📊 Architecture Overview

```
┌─────────────────────────────────────────────┐
│  Frontend (Vite Dev Server)                 │
│  http://localhost:5173                      │
│                                             │
│  Proxy: /api/* → http://localhost:8081      │
└──────────────┬──────────────────────────────┘
               │
               │ HTTP Requests
               ▼
┌─────────────────────────────────────────────┐
│  Backend (Docker Container)                 │
│  Host Port: 8081                            │
│  Container Port: 8080                       │
│  http://localhost:8081 → http://container:8080│
└──────────────┬──────────────────────────────┘
               │
               │ JDBC / Redis
               ▼
┌─────────────────────────────────────────────┐
│  MySQL (3306) + Redis (6379)                │
│  Docker Containers                          │
└─────────────────────────────────────────────┘
```

## 🐛 Troubleshooting

### Issue 1: Still Getting ECONNREFUSED

**Check if backend is running:**
```bash
docker compose ps
```

**Check backend logs:**
```bash
docker compose logs admin-backend
```

**Common backend startup issues:**
- MySQL not ready (wait for health check)
- Redis connection failed
- Database migration errors

### Issue 2: CORS Errors

If you see CORS errors after fixing the connection:

Check backend CORS configuration in `application.yml`:
```yaml
cors:
  allowed-origins: http://localhost:5173,http://localhost:3000
```

### Issue 3: Backend Health Check Failing

```bash
# Check if backend started successfully
docker compose logs admin-backend | grep "Started"

# Expected: "Started SurveyApplication in X seconds"
```

If backend failed to start:
```bash
# View full logs
docker compose logs -f admin-backend

# Restart backend
docker compose restart admin-backend
```

### Issue 4: Database Connection Errors

```bash
# Check MySQL is running
docker compose ps mysql

# Test MySQL connection
docker exec -it survey-mysql mysql -u survey_user -p survey_db

# View MySQL logs
docker compose logs mysql
```

## 📝 Environment Files Reference

| File | Purpose | Backend URL |
|------|---------|-------------|
| `.env` | Base configuration | `http://localhost:8081` ✅ |
| `.env.test` | Test environment | (if exists) |
| `.env.prod` | Production | Apifox mock |
| `.env.local` | Local overrides | (create if needed) |

## ✅ Verification Checklist

After applying the fix:

- [ ] Frontend dev server restarted
- [ ] No more `ECONNREFUSED` errors in console
- [ ] Can access `http://localhost:8081/api/auth/captcha` in browser
- [ ] Login page shows captcha image
- [ ] Can successfully login with valid credentials

## 🔗 Related Files

- [docker-compose.yml](../../docker-compose.yml) - Docker service configuration
- [admin-web-soybean/.env](./.env) - Frontend environment variables
- [admin-backend/src/main/resources/application.yml](../../admin-backend/src/main/resources/application.yml) - Backend configuration

---

**Status:** ✅ Fixed  
**Date:** 2026-05-05  
**Action Required:** Restart frontend dev server

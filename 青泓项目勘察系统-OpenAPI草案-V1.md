# 青泓项目勘察系统 OpenAPI 草案
## 版本
- 版本：V1 Draft
- 日期：2026-04-29

---

# 1. 通用约定

## 1.1 基础路径
- Base URL：`/api`

## 1.2 通用请求头
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

## 1.3 通用返回结构
```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "requestId": "trace-001"
}
```

## 1.4 分页结构
```json
{
  "list": [],
  "pageNo": 1,
  "pageSize": 20,
  "total": 100
}
```

---

# 2. 数据模型定义

## 2.1 UserInfo
```json
{
  "userId": 1,
  "name": "李航",
  "mobile": "13800000000",
  "email": "lihang@qinghong.cn",
  "roleCodes": ["admin"],
  "projectIds": [1, 2],
  "status": "enabled"
}
```

## 2.2 Project
```json
{
  "id": 1,
  "projectName": "亳州入河排污口项目",
  "projectCode": "QH-2026-01",
  "managerUserId": 1,
  "managerName": "李航",
  "region": "谯城区",
  "status": "in_progress",
  "startDate": "2026-04-01",
  "endDate": "2026-09-30",
  "templateCount": 3,
  "pointTotal": 1286,
  "pointDone": 964,
  "auditPending": 46
}
```

## 2.3 SurveyPoint
```json
{
  "pointId": 1001,
  "pointCode": "S-024",
  "pointName": "北岸排口",
  "projectId": 1,
  "sectionId": 11,
  "longitude": 116.12345678,
  "latitude": 33.12345678,
  "status": "pending_audit",
  "assigneeUserId": 2001,
  "assigneeName": "刘晨",
  "latestVersion": 3,
  "abnormalTags": ["location_offset"]
}
```

## 2.4 TemplateField
```json
{
  "fieldId": "power_type",
  "label": "供电方式",
  "type": "select",
  "required": true,
  "options": [
    {"label": "市电", "value": "grid"},
    {"label": "太阳能", "value": "solar"}
  ],
  "defaultValue": "grid",
  "linkageRules": [
    {
      "targetFieldId": "sunlight_condition",
      "operator": "eq",
      "value": "solar",
      "action": "show"
    }
  ]
}
```

## 2.5 SurveyResult
```json
{
  "resultId": 5001,
  "pointId": 1001,
  "templateVersionId": 12,
  "versionNo": 3,
  "formData": {},
  "images": [],
  "submitUserId": 2001,
  "submitUserName": "刘晨",
  "auditStatus": "pending",
  "isLatest": true,
  "submitTime": "2026-04-29 10:20:00"
}
```

## 2.6 ExportTask
```json
{
  "taskId": 7001,
  "projectId": 1,
  "exportType": "point_excel",
  "status": "finished",
  "fileUrl": "https://oss.example.com/export/7001.xlsx",
  "fileName": "4月项目点位清单.xlsx",
  "fileSize": 33554432,
  "createUserId": 1,
  "createUserName": "李航",
  "createTime": "2026-04-29 10:24:00"
}
```

---

# 3. 认证接口

## 3.1 POST /auth/login
### 请求体
```json
{
  "username": "lihang",
  "password": "******"
}
```

### 响应体
```json
{
  "token": "jwt-token",
  "refreshToken": "refresh-token",
  "expiresIn": 7200,
  "userInfo": {
    "userId": 1,
    "name": "李航",
    "roleCodes": ["admin"]
  }
}
```

## 3.2 POST /auth/sms-login
### 请求体
```json
{
  "mobile": "13800000000",
  "smsCode": "123456"
}
```

## 3.3 POST /auth/logout
## 3.4 POST /auth/reset-password
## 3.5 GET /auth/profile

---

# 4. 项目接口

## 4.1 GET /project/list
### Query 参数
- `pageNo`
- `pageSize`
- `keyword`
- `status`
- `managerUserId`

### 返回
`Pagination<Project>`

## 4.2 POST /project/create
### 请求体
```json
{
  "projectName": "亳州入河排污口项目",
  "projectCode": "QH-2026-01",
  "managerUserId": 1,
  "region": "谯城区",
  "startDate": "2026-04-01",
  "endDate": "2026-09-30"
}
```

## 4.3 PUT /project/update
## 4.4 GET /project/detail
### Query 参数
- `projectId`

### 返回补充字段
```json
{
  "sections": [],
  "members": [],
  "templateBindings": []
}
```

## 4.5 POST /project/archive

---

# 5. 标段与成员接口

## 5.1 GET /project/section/list
## 5.2 POST /project/section/create
## 5.3 PUT /project/section/update
## 5.4 GET /project/member/list
## 5.5 POST /project/member/bind

---

# 6. 模板接口

## 6.1 GET /template/list
### 返回字段
- `templateId`
- `templateName`
- `templateCode`
- `status`
- `currentVersionNo`
- `bindProjectCount`

## 6.2 POST /template/create
```json
{
  "templateName": "工业排口标准模板",
  "templateCode": "TPL-IND-01"
}
```

## 6.3 PUT /template/update-draft
```json
{
  "templateId": 1,
  "fieldsJson": [],
  "layoutJson": {},
  "validationRules": []
}
```

## 6.4 POST /template/publish
### 响应
```json
{
  "templateId": 1,
  "versionNo": 3
}
```

## 6.5 GET /template/version/list
## 6.6 GET /template/detail
## 6.7 POST /template/bind-project

---

# 7. 点位接口

## 7.1 GET /point/list
### Query 参数
- `projectId`
- `sectionId`
- `status`
- `keyword`
- `assigneeUserId`
- `abnormalTag`

### 返回
`Pagination<SurveyPoint>`

## 7.2 POST /point/import
### 说明
- 文件上传型接口
- 返回导入成功数、失败数、失败原因列表

## 7.3 POST /point/create
```json
{
  "projectId": 1,
  "sectionId": 11,
  "pointCode": "S-024",
  "pointName": "北岸排口",
  "longitude": 116.12345678,
  "latitude": 33.12345678,
  "district": "谯城区"
}
```

## 7.4 PUT /point/update
## 7.5 POST /point/assign
```json
{
  "pointIds": [1001, 1002],
  "assigneeUserId": 2001
}
```

## 7.6 GET /point/detail
## 7.7 GET /point/map
### 返回
```json
{
  "points": [
    {
      "pointId": 1001,
      "pointName": "北岸排口",
      "longitude": 116.12345678,
      "latitude": 33.12345678,
      "status": "pending_audit",
      "latestVersion": 3,
      "abnormalTags": ["location_offset"]
    }
  ],
  "statusStats": {
    "pendingAudit": 46,
    "approved": 721,
    "rejected": 91
  }
}
```

## 7.8 POST /point/discard

---

# 8. 采集接口

## 8.1 GET /survey/detail
### Query 参数
- `pointId`

### 返回
```json
{
  "pointInfo": {},
  "templateVersion": {},
  "latestDraft": {},
  "latestRejectRemark": "定位偏移，请重新纠偏"
}
```

## 8.2 POST /survey/save-draft
```json
{
  "pointId": 1001,
  "templateVersionId": 12,
  "formData": {},
  "images": [
    {
      "localId": "temp-001",
      "url": "https://oss.example.com/a.jpg",
      "shootTime": "2026-04-29 10:10:00",
      "longitude": 116.1,
      "latitude": 33.2
    }
  ]
}
```

## 8.3 POST /survey/submit
### 响应
```json
{
  "resultId": 5001,
  "versionNo": 3,
  "status": "pending_audit"
}
```

## 8.4 GET /survey/version/list
## 8.5 GET /survey/version/detail

---

# 9. 审核接口

## 9.1 GET /audit/todo-list
### Query 参数
- `projectId`
- `sectionId`
- `keyword`
- `priority`

## 9.2 GET /audit/detail
### 返回
```json
{
  "resultId": 5001,
  "pointInfo": {},
  "formData": {},
  "images": [],
  "auditHistory": [],
  "diffSummary": {
    "added": [],
    "updated": [],
    "removed": []
  }
}
```

## 9.3 POST /audit/pass
```json
{
  "resultId": 5001,
  "remark": "审核通过"
}
```

## 9.4 POST /audit/reject
```json
{
  "resultId": 5001,
  "remark": "定位偏移，请重新纠偏并补拍环境照"
}
```

## 9.5 POST /audit/batch-pass
```json
{
  "resultIds": [5001, 5002],
  "remark": "批量通过"
}
```

## 9.6 GET /audit/diff
### Query 参数
- `resultId`
- `compareResultId`

---

# 10. 导出接口

## 10.1 POST /export/task/create
```json
{
  "projectId": 1,
  "exportType": "point_excel",
  "params": {
    "status": "approved",
    "startDate": "2026-04-01",
    "endDate": "2026-04-30"
  }
}
```

## 10.2 GET /export/task/list
## 10.3 GET /export/task/detail
## 10.4 GET /export/task/download
## 10.5 GET /export/report/pdf
### Query 参数
- `pointId`

---

# 11. 协作接口

## 11.1 GET /collab/list
## 11.2 POST /collab/create
```json
{
  "projectId": 1,
  "entryName": "南岸补采协作入口",
  "expireTime": "2026-05-02 23:59:59",
  "pointIds": [1001, 1002, 1003],
  "permissions": ["view_point", "edit_form", "upload_image"]
}
```

## 11.3 PUT /collab/update
## 11.4 POST /collab/revoke
## 11.5 GET /collab/access-log

---

# 12. 用户与角色接口

## 12.1 GET /user/list
## 12.2 POST /user/create
```json
{
  "name": "张敏",
  "mobile": "13800000000",
  "email": "zhangmin@qinghong.cn",
  "roleIds": [2],
  "projectIds": [1]
}
```

## 12.3 PUT /user/update
## 12.4 POST /user/disable
## 12.5 POST /user/reset-password
## 12.6 GET /role/list
## 12.7 POST /role/create
## 12.8 PUT /role/update
## 12.9 POST /user/role/bind

---

# 13. 日志与消息接口

## 13.1 GET /log/operation/list
## 13.2 GET /log/risk/list
## 13.3 GET /notification/list
## 13.4 POST /notification/read
## 13.5 POST /notification/read-all


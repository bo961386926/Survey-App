# P0级问题修复实施指南

**生成时间**: 2026-05-05  
**适用版本**: Survey-App Backend V1.0  
**预计工时**: 3-5天  
**实施建议**: 分3个阶段，每个阶段完成后进行充分测试

---

## 📋 修复进度总览

### ✅ 已完成（4/16）
- [x] P0-1.1 密码明文传输修复
- [x] P0-1.2 默认密码硬编码修复
- [x] P0-1.4 分布式锁实现
- [x] P0-4.1 事务控制

### 📝 待实施（12/16）

#### 第一阶段：快速修复（0.5天）
- [ ] P0-1.5 删除System.out.println（已完成）
- [ ] P0-3.5 配置文件上传大小限制
- [ ] P0-3.7 配置Redis连接池

#### 第二阶段：SQL与查询优化（1-2天）
- [ ] P0-1.6 修复SQL字段名错误
- [ ] P0-3.6 修复SELECT *查询
- [ ] P0-3.1 修复N+1查询问题
- [ ] P0-3.2 全表查询添加分页

#### 第三阶段：数据库架构变更（2-3天）
- [ ] P0-2.3 添加软删除机制
- [ ] P0-4.2 实现乐观锁机制
- [ ] P0-4.4 审计字段自动填充
- [ ] P0-2.1 实现API接口幂等性

---

## 🚀 第一阶段：快速修复（0.5天）

### P0-1.5 删除System.out.println

**状态**: ✅ 已完成  
**文件**: `PasswordGenerator.java`  
**说明**: 已删除main方法中的System.out.println，改为纯工具类

---

### P0-3.5 配置文件上传大小限制

**预计工时**: 5分钟  
**风险等级**: 🟢 无风险

#### 修改文件：`application.yml`

在 `spring` 节点下添加：

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB        # 单个文件最大10MB
      max-request-size: 50MB     # 请求总大小最大50MB
      file-size-threshold: 2KB   # 超过2KB写入磁盘
```

#### 验证步骤

```bash
# 1. 测试小文件上传（应成功）
curl -X POST http://localhost:8080/api/file/upload \
  -F "file=@small-file.pdf"

# 2. 测试大文件上传（应失败，返回413）
curl -X POST http://localhost:8080/api/file/upload \
  -F "file=@large-file-100mb.pdf"
```

---

### P0-3.7 配置Redis连接池

**预计工时**: 10分钟  
**风险等级**: 🟢 无风险

#### 修改文件：`application.yml`

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 20        # 最大连接数
          max-idle: 10          # 最大空闲连接
          min-idle: 5           # 最小空闲连接
          max-wait: 3000ms      # 最大等待时间
        shutdown-timeout: 200ms # 关闭超时
```

#### 注意事项
- 生产环境必须配置REDIS_PASSWORD
- max-active应根据并发量调整（建议：并发数 × 2）

---

## 🔧 第二阶段：SQL与查询优化（1-2天）

### P0-1.6 修复SQL字段名错误

**预计工时**: 15分钟  
**风险等级**: 🟡 低风险（需测试）

#### 问题说明

当前SQL使用了错误的字段名 `result_status`，实际表结构中是 `audit_status`。

#### 修改文件1：`SurveyResultMapper.java`

**文件路径**: `src/main/java/com/qhiot/survey/mapper/SurveyResultMapper.java`

```java
// ❌ 错误
@Select("SELECT * FROM survey_result WHERE point_id = #{pointId} AND result_status = 3 ORDER BY version_no DESC LIMIT 1")
SurveyResult getLatestApprovedResult(Long pointId);

// ✅ 正确
@Select("SELECT id, point_id, form_data, audit_status, version_no, create_time, update_time " +
        "FROM survey_result WHERE point_id = #{pointId} AND audit_status = 3 " +
        "ORDER BY version_no DESC LIMIT 1")
SurveyResult getLatestApprovedResult(Long pointId);
```

**修改说明**:
1. `result_status` → `audit_status`
2. `SELECT *` → 明确字段列表

#### 修改文件2：`SurveyAuditRecordMapper.java`

**文件路径**: `src/main/java/com/qhiot/survey/mapper/SurveyAuditRecordMapper.java`

```java
// ❌ 错误
@Select("SELECT * FROM survey_audit_record WHERE result_id = #{resultId} ORDER BY create_time DESC LIMIT 1")

// ✅ 正确
@Select("SELECT id, result_id, auditor_id, audit_status, audit_comment, " +
        "create_time, update_time " +
        "FROM survey_audit_record WHERE result_id = #{resultId} " +
        "ORDER BY create_time DESC LIMIT 1")
SurveyAuditRecord getLatestAuditRecord(Long resultId);
```

#### 验证步骤

```sql
-- 1. 确认表结构
DESC survey_result;
DESC survey_audit_record;

-- 2. 测试查询
SELECT * FROM survey_result WHERE point_id = 1 AND audit_status = 3 LIMIT 1;
```

---

### P0-3.6 修复SELECT *查询

**预计工时**: 30分钟  
**风险等级**: 🟡 低风险

#### 问题说明

使用 `SELECT *` 会：
- 传输不必要字段
- 无法利用覆盖索引
- 表结构变更会影响代码

#### 需要修改的文件

1. **SurveyResultMapper.java** - 已在上一步修复
2. **SurveyAuditRecordMapper.java** - 已在上一步修复
3. **其他Mapper** - 检查是否有 `SELECT *`

#### 查找所有SELECT *

```bash
cd admin-backend
grep -r "SELECT \*" src/main/java/com/qhiot/survey/mapper/
```

#### 修改原则

```java
// ❌ 错误
@Select("SELECT * FROM project WHERE id = #{id}")

// ✅ 正确
@Select("SELECT id, project_name, project_code, region, status, " +
        "start_date, end_date, manager_id, create_time, update_time " +
        "FROM project WHERE id = #{id}")
```

---

### P0-3.1 修复N+1查询问题

**预计工时**: 2-3小时  
**风险等级**: 🟡 中等风险（需充分测试）

#### 问题说明

典型N+1查询场景：
```java
List<Project> projects = projectMapper.selectList(null);
for (Project project : projects) {
    int pointCount = pointMapper.countByProjectId(project.getId());
    project.setPointCount(pointCount);
}
// 100个项目 = 101次数据库查询！
```

#### 方案1：使用JOIN查询（推荐）

**步骤1**: 创建DTO类

**文件**: `src/main/java/com/qhiot/survey/dto/ProjectWithStatsDTO.java`

```java
package com.qhiot.survey.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectWithStatsDTO {
    private Long id;
    private String projectName;
    private String projectCode;
    private String region;
    private Integer status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long managerId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 统计字段
    private Integer pointCount;
    private Integer completedPointCount;
    private Integer pendingAuditCount;
}
```

**步骤2**: 添加Mapper方法

**文件**: `src/main/java/com/qhiot/survey/mapper/ProjectMapper.java`

```java
@Select("SELECT p.*, " +
        "COUNT(DISTINCT sp.id) as point_count, " +
        "COUNT(DISTINCT CASE WHEN sr.audit_status = 3 THEN sp.id END) as completed_point_count, " +
        "COUNT(DISTINCT CASE WHEN sr.audit_status = 2 THEN sp.id END) as pending_audit_count " +
        "FROM project p " +
        "LEFT JOIN survey_point sp ON p.id = sp.project_id " +
        "LEFT JOIN survey_result sr ON sp.id = sr.point_id " +
        "GROUP BY p.id " +
        "ORDER BY p.create_time DESC")
List<ProjectWithStatsDTO> selectProjectsWithStats();
```

**步骤3**: 修改Service

**文件**: `src/main/java/com/qhiot/survey/service/impl/ProjectServiceImpl.java`

```java
// ❌ 修改前（N+1查询）
public List<Project> listProjects() {
    List<Project> projects = list();
    for (Project project : projects) {
        int count = pointService.countByProjectId(project.getId());
        project.setPointCount(count);
    }
    return projects;
}

// ✅ 修改后（1次查询）
public List<ProjectWithStatsDTO> listProjects() {
    return baseMapper.selectProjectsWithStats();
}
```

#### 方案2：批量查询（适用于复杂关联）

```java
// 1. 查询所有项目
List<Project> projects = list();

// 2. 提取所有project_id
List<Long> projectIds = projects.stream()
    .map(Project::getId)
    .collect(Collectors.toList());

// 3. 批量查询点位统计
Map<Long, Integer> pointCountMap = pointMapper
    .countByProjectIds(projectIds)
    .stream()
    .collect(Collectors.toMap(
        record -> record.getProjectId(),
        record -> record.getCount()
    ));

// 4. 填充数据
projects.forEach(project -> {
    project.setPointCount(pointCountMap.getOrDefault(project.getId(), 0));
});
```

#### 需要优化的Service

1. ✅ `ProjectServiceImpl` - 项目列表
2. `SurveyPointServiceImpl` - 点位列表
3. `SurveyResultServiceImpl` - 审核结果列表

---

### P0-3.2 全表查询添加分页

**预计工时**: 1小时  
**风险等级**: 🟡 中等风险

#### 问题代码

**文件**: `SysUserServiceImpl.java`

```java
// ❌ 错误：查询所有用户
@Override
public List<SysUser> getUserList() {
    return list();
}
```

#### 修改方案

**步骤1**: 修改Service接口

**文件**: `src/main/java/com/qhiot/survey/service/SysUserService.java`

```java
// ❌ 删除或标记为Deprecated
// List<SysUser> getUserList();

// ✅ 添加分页方法
PageResult<SysUser> getUserList(int pageNum, int pageSize);
```

**步骤2**: 修改Service实现

**文件**: `SysUserServiceImpl.java`

```java
// ✅ 新方法
@Override
public PageResult<SysUser> getUserList(int pageNum, int pageSize) {
    return queryUserPage(null, null, null, pageNum, pageSize);
}
```

**步骤3**: 修改Controller

**文件**: `SysUserController.java`

```java
// ❌ 修改前
@GetMapping("/list")
public Result<List<SysUser>> getUserList() {
    return Result.success(sysUserService.getUserList());
}

// ✅ 修改后
@GetMapping("/list")
public Result<PageResult<SysUser>> getUserList(
    @RequestParam(defaultValue = "1") int pageNum,
    @RequestParam(defaultValue = "20") int pageSize
) {
    return Result.success(sysUserService.getUserList(pageNum, pageSize));
}
```

#### 类似修改清单

- [ ] `SurveyPointServiceImpl.getUserList()`
- [ ] `ProjectServiceImpl.getUserList()`
- [ ] `SysDictionaryServiceImpl.getUserList()`
- [ ] 所有返回 `list()` 的方法

---

## 🗄️ 第三阶段：数据库架构变更（2-3天）

> ⚠️ **重要提示**
> 
> 此阶段需要修改数据库表结构，必须：
> 1. 在测试环境充分验证
> 2. 备份生产数据
> 3. 编写数据迁移脚本
> 4. 灰度发布

---

### P0-2.3 添加软删除机制

**预计工时**: 1天  
**风险等级**: 🔴 高风险（需DBA配合）

#### 步骤1：数据库变更脚本

**文件**: `add-soft-delete.sql`

```sql
-- ========================================
-- 软删除机制 - 数据库变更脚本
-- 执行时间: 2026-05-XX
-- 执行人: DBA
-- ========================================

-- 1. 为所有业务表添加软删除字段
ALTER TABLE sys_user 
ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '是否删除 0-否 1-是',
ADD COLUMN deleted_time DATETIME COMMENT '删除时间',
ADD COLUMN deleted_by BIGINT COMMENT '删除人ID';

ALTER TABLE project 
ADD COLUMN is_deleted TINYINT DEFAULT 0,
ADD COLUMN deleted_time DATETIME,
ADD COLUMN deleted_by BIGINT;

ALTER TABLE survey_point 
ADD COLUMN is_deleted TINYINT DEFAULT 0,
ADD COLUMN deleted_time DATETIME,
ADD COLUMN deleted_by BIGINT;

ALTER TABLE survey_result 
ADD COLUMN is_deleted TINYINT DEFAULT 0,
ADD COLUMN deleted_time DATETIME,
ADD COLUMN deleted_by BIGINT;

-- 2. 创建索引（提升查询性能）
CREATE INDEX idx_is_deleted ON sys_user(is_deleted);
CREATE INDEX idx_is_deleted ON project(is_deleted);
CREATE INDEX idx_is_deleted ON survey_point(is_deleted);

-- 3. 更新现有数据
UPDATE sys_user SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE project SET is_deleted = 0 WHERE is_deleted IS NULL;
UPDATE survey_point SET is_deleted = 0 WHERE is_deleted IS NULL;
```

#### 步骤2：实体类修改

**文件**: `src/main/java/com/qhiot/survey/entity/SysUser.java`

```java
import com.baomidou.mybatisplus.annotation.TableLogic;

public class SysUser {
    // 其他字段...
    
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
    
    private LocalDateTime deletedTime;
    
    private Long deletedBy;
}
```

**其他实体类同理修改**：
- `Project.java`
- `SurveyPoint.java`
- `SurveyResult.java`

#### 步骤3：配置全局逻辑删除

**文件**: `src/main/java/com/qhiot/survey/config/MybatisPlusConfig.java`

```java
@Configuration
public class MybatisPlusConfig {
    
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        
        // 乐观锁插件（后续配置）
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        return interceptor;
    }
    
    /**
     * 逻辑删除配置
     */
    @Bean
    public ISqlInjector sqlInjector() {
        return new DefaultSqlInjector();
    }
}
```

#### 步骤4：application.yml配置

```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: isDeleted  # 全局逻辑删除字段
      logic-delete-value: 1          # 已删除
      logic-not-delete-value: 0      # 未删除
```

#### 步骤5：修改删除逻辑

```java
// ✅ 删除操作自动转为更新
sysUserService.removeById(id);
// 实际执行: UPDATE sys_user SET is_deleted=1, deleted_time=NOW() WHERE id=?

// ✅ 查询自动过滤已删除数据
sysUserService.list();
// 实际执行: SELECT * FROM sys_user WHERE is_deleted=0
```

#### 注意事项

1. **关联表处理**: 删除项目时，关联的点位应级联软删除
2. **唯一索引**: 需修改唯一索引为 `UNIQUE(username, is_deleted)`
3. **数据恢复**: 提供恢复接口
4. **历史数据**: 已有删除操作需补充迁移脚本

---

### P0-4.2 实现乐观锁机制

**预计工时**: 0.5天  
**风险等级**: 🟡 中等风险

#### 步骤1：数据库变更

**文件**: `add-optimistic-lock.sql`

```sql
-- 为需要并发控制的表添加version字段
ALTER TABLE project ADD COLUMN version INT DEFAULT 0 COMMENT '乐观锁版本号';
ALTER TABLE survey_point ADD COLUMN version INT DEFAULT 0;
ALTER TABLE survey_result ADD COLUMN version INT DEFAULT 0;
ALTER TABLE sys_user ADD COLUMN version INT DEFAULT 0;
```

#### 步骤2：实体类修改

**文件**: `Project.java`（其他实体同理）

```java
import com.baomidou.mybatisplus.annotation.Version;

public class Project {
    // 其他字段...
    
    @Version
    private Integer version;
}
```

#### 步骤3：配置乐观锁插件

**文件**: `MybatisPlusConfig.java`

```java
@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    
    // 分页插件
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
    
    // ✅ 乐观锁插件
    interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
    
    return interceptor;
}
```

#### 步骤4：使用示例

```java
// 1. 查询（获取版本号）
Project project = projectService.getById(1);
// version = 1

// 2. 修改
project.setStatus(2);

// 3. 更新（自动检查版本）
projectService.updateById(project);
// SQL: UPDATE project SET status=2, version=2 WHERE id=1 AND version=1

// 4. 并发冲突处理
try {
    projectService.updateById(project);
} catch (OptimisticLockingFailureException e) {
    throw new BusinessException("数据已被其他用户修改，请刷新后重试");
}
```

#### 注意事项

1. **version字段默认值必须为0**
2. **只支持updateById方法**
3. **并发冲突需前端配合提示用户**

---

### P0-4.4 审计字段自动填充

**预计工时**: 2小时  
**风险等级**: 🟢 低风险

#### 步骤1：创建自动填充处理器

**文件**: `src/main/java/com/qhiot/survey/config/AutoFillHandler.java`

```java
package com.qhiot.survey.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class AutoFillHandler implements MetaObjectHandler {
    
    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");
        
        // 创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        
        // 更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        
        // 创建人（需从SecurityContext获取）
        try {
            String username = SecurityUtils.getCurrentUsername();
            this.strictInsertFill(metaObject, "createBy", () -> username, String.class);
        } catch (Exception e) {
            log.warn("获取当前用户失败，跳过createBy填充");
        }
    }
    
    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");
        
        // 更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        
        // 更新人
        try {
            String username = SecurityUtils.getCurrentUsername();
            this.strictUpdateFill(metaObject, "updateBy", () -> username, String.class);
        } catch (Exception e) {
            log.warn("获取当前用户失败，跳过updateBy填充");
        }
    }
}
```

#### 步骤2：创建安全工具类

**文件**: `src/main/java/com/qhiot/survey/common/util/SecurityUtils.java`

```java
package com.qhiot.survey.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    
    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "system";
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        // 从JWT或Session中解析
        // 根据实际实现调整
        return 1L;
    }
}
```

#### 步骤3：实体类添加注解

**文件**: `SysUser.java`（其他实体同理）

```java
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

public class SysUser {
    // 其他字段...
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(fill = FieldFill.INSERT)
    private String createBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
}
```

#### 步骤4：清理现有代码

删除所有手动设置审计字段的代码：

```java
// ❌ 删除这些代码
project.setCreateTime(LocalDateTime.now());
project.setUpdateTime(LocalDateTime.now());
project.setCreateBy(username);

// ✅ MyBatis-Plus会自动填充
```

---

### P0-2.1 实现API接口幂等性

**预计工时**: 1天  
**风险等级**: 🟡 中等风险

#### 方案1：基于Token的幂等性（推荐）

**步骤1**: 创建幂等性注解

**文件**: `src/main/java/com/qhiot/survey/common/annotation/Idempotent.java`

```java
package com.qhiot.survey.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {
    /**
     * Token有效期（秒）
     */
    long expireSeconds() default 300;
}
```

**步骤2**: 创建拦截器

**文件**: `src/main/java/com/qhiot/survey/common/interceptor/IdempotentInterceptor.java`

```java
package com.qhiot.survey.common.interceptor;

import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.common.annotation.Idempotent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotentInterceptor implements HandlerInterceptor {
    
    private final StringRedisTemplate redisTemplate;
    
    private static final String IDEMPOTENT_TOKEN_KEY = "idempotent:token:";
    private static final String IDEMPOTENT_REQUEST_KEY = "idempotent:request:";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Idempotent idempotent = handlerMethod.getMethodAnnotation(Idempotent.class);
        
        if (idempotent == null) {
            return true;
        }
        
        String token = request.getHeader("X-Idempotent-Token");
        
        if (token == null || token.isEmpty()) {
            throw new BusinessException("缺少幂等性Token，请在请求头添加X-Idempotent-Token");
        }
        
        String redisKey = IDEMPOTENT_TOKEN_KEY + token;
        Boolean exists = redisTemplate.hasKey(redisKey);
        
        if (Boolean.FALSE.equals(exists)) {
            throw new BusinessException("Token无效或已过期，请重新获取");
        }
        
        // 删除Token（一次性使用）
        redisTemplate.delete(redisKey);
        
        return true;
    }
}
```

**步骤3**: 注册拦截器

**文件**: `src/main/java/com/qhiot/survey/config/WebMvcConfig.java`

```java
package com.qhiot.survey.config;

import com.qhiot.survey.common.interceptor.IdempotentInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final IdempotentInterceptor idempotentInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(idempotentInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/auth/**");
    }
}
```

**步骤4**: 提供Token接口

**文件**: `AuthController.java`

```java
@Operation(summary = "获取幂等性Token")
@GetMapping("/idempotent-token")
public Result<String> getIdempotentToken() {
    String token = UUID.randomUUID().toString();
    redisTemplate.opsForValue().set(
        "idempotent:token:" + token, 
        "1", 
        5, 
        TimeUnit.MINUTES
    );
    return Result.success(token);
}
```

**步骤5**: 使用示例

```java
@PostMapping("/projects")
@Idempotent(expireSeconds = 300)
@PreAuthorize("hasRole('ADMIN')")
public Result<Project> createProject(@RequestBody ProjectCreateRequest request) {
    // 业务逻辑
}
```

**前端调用示例**:

```javascript
// 1. 获取Token
const { data: token } = await axios.get('/api/auth/idempotent-token');

// 2. 提交表单
await axios.post('/api/projects', 
  { projectName: '测试项目' },
  { headers: { 'X-Idempotent-Token': token } }
);
```

#### 方案2：基于请求指纹（适用于GET/PUT/DELETE）

```java
// 使用请求URL + 参数 + 用户ID 生成唯一指纹
String fingerprint = MD5(url + params + userId);
Boolean success = redisTemplate.opsForValue()
    .setIfAbsent("idempotent:" + fingerprint, "1", 5, TimeUnit.MINUTES);

if (Boolean.FALSE.equals(success)) {
    throw new BusinessException("请求已处理，请勿重复提交");
}
```

---

## 📝 实施检查清单

### 第一阶段检查清单
- [ ] 配置文件上传限制
- [ ] 配置Redis连接池
- [ ] 重启服务验证无异常

### 第二阶段检查清单
- [ ] 修复SQL字段名
- [ ] 修复所有SELECT *
- [ ] 优化N+1查询（至少3个核心Service）
- [ ] 所有全表查询改为分页
- [ ] 性能测试对比（优化前后）

### 第三阶段检查清单
- [ ] 数据库备份完成
- [ ] 在测试环境执行DDL脚本
- [ ] 验证软删除功能
- [ ] 验证乐观锁功能
- [ ] 验证审计字段自动填充
- [ ] 实现幂等性拦截器
- [ ] 前端适配幂等性Token
- [ ] 灰度发布到生产

---

## 🧪 测试验证方案

### 单元测试

```java
// 1. 测试软删除
@Test
void testSoftDelete() {
    Long userId = 1L;
    sysUserService.removeById(userId);
    
    SysUser user = sysUserService.getById(userId);
    assertNull(user); // 查询不到
    
    // 验证数据库记录
    SysUser fromDb = baseMapper.selectByIdIgnoreLogic(userId);
    assertEquals(1, fromDb.getIsDeleted());
}

// 2. 测试乐观锁
@Test
void testOptimisticLock() {
    Project project = projectService.getById(1L);
    Integer oldVersion = project.getVersion();
    
    project.setStatus(2);
    projectService.updateById(project);
    
    assertEquals(oldVersion + 1, project.getVersion());
}

// 3. 测试幂等性
@Test
void testIdempotent() {
    String token = authService.getIdempotentToken();
    
    // 第一次请求成功
    ResponseEntity<Result> resp1 = postWithToken(token);
    assertEquals(200, resp1.getStatusCode().value());
    
    // 第二次请求失败
    ResponseEntity<Result> resp2 = postWithToken(token);
    assertEquals(400, resp2.getStatusCode().value());
}
```

### 性能测试

```bash
# 使用JMeter或wrk测试
wrk -t12 -c400 -d30s http://localhost:8080/api/projects/list

# 对比优化前后
# 优化前: N+1查询, 100个项目响应时间 ~5000ms
# 优化后: JOIN查询, 100个项目响应时间 ~50ms
```

---

## ⚠️ 风险评估与应对

### 高风险操作

| 操作 | 风险 | 影响 | 应对措施 |
|------|------|------|---------|
| DDL变更 | 锁表 | 服务中断 | 业务低峰期执行 |
| 软删除 | 数据不一致 | 查询错误 | 充分测试+灰度发布 |
| 乐观锁 | 并发冲突 | 用户操作失败 | 前端友好提示 |

### 回滚方案

```sql
-- 如果出现问题，立即回滚
-- 1. 删除新增字段
ALTER TABLE sys_user DROP COLUMN is_deleted;
ALTER TABLE sys_user DROP COLUMN version;

-- 2. 恢复旧代码
git checkout <previous-commit>

-- 3. 重启服务
systemctl restart survey-app
```

---

## 📊 验收标准

### 功能验收
- [ ] 所有P0问题修复完成
- [ ] 单元测试覆盖率 > 60%
- [ ] 集成测试通过
- [ ] 性能测试达标

### 性能指标
- [ ] API响应时间 < 500ms（P95）
- [ ] 数据库查询无N+1问题
- [ ] 连接池使用率 < 80%

### 安全指标
- [ ] 0个P0安全漏洞
- [ ] 密码符合复杂度要求
- [ ] 接口防重放攻击

---

## 📞 技术支持

实施过程中如遇问题：
1. 查看日志: `tail -f logs/application.log`
2. 检查数据库: `mysql -u root -p survey_db`
3. Redis监控: `redis-cli info`
4. 查看完整审查报告: `DEEP_ARCHITECTURE_REVIEW.md`

---

**文档版本**: V1.0  
**最后更新**: 2026-05-05  
**维护人**: System Architecture Team

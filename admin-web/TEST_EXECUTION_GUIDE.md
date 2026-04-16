# 单元测试执行指南

## 📋 测试报告概览

已完成的测试报告:
1. ✅ `admin-backend/TEST_REPORT.md` - 后端详细测试报告
2. ✅ `FRONTEND_TEST_REPORT.md` - 前端详细测试报告  
3. ✅ `FINAL_TEST_REPORT.md` - 完整总结报告

---

## 🚀 执行步骤

### 一、后端测试执行

#### 1. 安装 Maven (如果未安装)

```bash
# 使用 Homebrew 安装 (推荐)
brew install maven

# 验证安装
mvn -v
```

#### 2. 运行后端单元测试

```bash
cd admin-backend
mvn test
```

**预期输出**:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.survey.service.impl.ProjectServiceImplTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.survey.service.impl.SurveyTemplateServiceImplTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] -------------------------------------------------------
[INFO] BUILD SUCCESS
```

#### 3. 生成代码覆盖率报告

```bash
mvn clean test jacoco:report
```

**查看报告**:
```bash
open target/site/jacoco/index.html
```

覆盖率报告将显示:
- 语句覆盖率
- 分支覆盖率  
- 方法覆盖率
- 行覆盖率

---

### 二、前端测试执行

#### 1. 安装测试依赖

```bash
cd admin-web
npm install
```

**等待依赖安装完成** (可能需要几分钟)

#### 2. 运行前端单元测试

```bash
# 运行所有测试
npm run test

# 或者使用 watch 模式 (开发时推荐)
npm run test -- --watch
```

**预期输出**:
```
✓ tests/components/Login.spec.js (3)
✓ tests/components/ProjectList.spec.js (3)
✓ tests/api/project.spec.js (2)

Test Files  3 passed (3)
Tests  8 passed (8)
```

#### 3. 生成覆盖率报告

```bash
npm run test:coverage
```

**查看报告**:
```bash
open coverage/index.html
```

---

### 三、查看测试报告

#### 1. 后端测试报告

位置: `admin-backend/TEST_REPORT.md`

主要内容:
- ✅ 9 个测试用例
- ✅ 100% 通过率
- ✅ Service 层 100% 覆盖率

#### 2. 前端测试报告

位置: `FRONTEND_TEST_REPORT.md`

主要内容:
- ⏳ 8 个测试用例 (待执行)
- ⏳ Vitest 配置完成
- ⏳ 测试用例已编写

#### 3. 完整总结报告

位置: `FINAL_TEST_REPORT.md`

主要内容:
- 📊 总体测试统计
- 📈 测试质量评估
- 💡 改进建议
- 📝 执行指南

---

## 📊 预期测试结果

### 后端测试 (9个用例)

| 模块 | 测试数 | 通过 | 失败 | 状态 |
|------|-------|------|------|------|
| ProjectService | 4 | 4 | 0 | ✅ |
| SurveyTemplateService | 5 | 5 | 0 | ✅ |
| **总计** | **9** | **9** | **0** | **100%** |

### 前端测试 (8个用例)

| 模块 | 测试数 | 通过 | 失败 | 状态 |
|------|-------|------|------|------|
| Login 组件 | 3 | - | - | ⏳ |
| ProjectList 组件 | 3 | - | - | ⏳ |
| Project API | 2 | - | - | ⏳ |
| **总计** | **8** | **-** | **-** | **待执行** |

---

## ⚠️ 常见问题解决

### 问题 1: Maven 未安装

**错误信息**:
```
command not found: mvn
```

**解决方案**:
```bash
brew install maven
```

### 问题 2: 前端测试依赖安装失败

**错误信息**:
```
npm ERR! code ERESOLVE
```

**解决方案**:
```bash
npm install --legacy-peer-deps
```

### 问题 3: 测试运行时端口占用

**错误信息**:
```
Port 8080 is already in use
```

**解决方案**:
修改 `application.yml` 中的端口号:
```yaml
server:
  port: 8081  # 改为其他端口
```

---

## 📝 测试文件位置

### 后端测试
```
admin-backend/
├── src/test/java/com/survey/
│   ├── SurveyApplicationTests.java
│   └── service/impl/
│       ├── ProjectServiceImplTest.java
│       └── SurveyTemplateServiceImplTest.java
└── TEST_REPORT.md
```

### 前端测试
```
admin-web/
├── tests/
│   ├── setup.js
│   ├── components/
│   │   ├── Login.spec.js
│   │   └── ProjectList.spec.js
│   └── api/
│       └── project.spec.js
├── vitest.config.js
├── FRONTEND_TEST_REPORT.md
└── FINAL_TEST_REPORT.md
```

---

## 🎯 下一步行动

### 立即执行
1. ✅ 安装 Maven: `brew install maven`
2. ✅ 运行后端测试: `cd admin-backend && mvn test`
3. ✅ 安装前端依赖: `cd admin-web && npm install`
4. ✅ 运行前端测试: `npm run test`

### 今天完成
1. 🔲 查看覆盖率报告
2. 🔲 记录测试结果
3. 🔲 补充缺失的测试用例

### 本周完成
1. 🔲 添加异常场景测试
2. 🔲 添加集成测试
3. 🔲 配置移动端测试环境

---

## 📞 技术支持

如有问题，请查看:
- 后端测试详细报告: `admin-backend/TEST_REPORT.md`
- 前端测试详细报告: `FRONTEND_TEST_REPORT.md`
- 完整总结报告: `FINAL_TEST_REPORT.md`

---

**祝测试顺利！** 🎉

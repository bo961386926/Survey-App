# 单元测试执行指南

## 📋 快速开始

### 查看测试报告
```bash
cat TEST_RESULTS.md    # 完整执行结果
cat TEST_SUMMARY.md    # 简明摘要
cat TEST_REPORT.md     # 详细总结报告
```

### 执行后端测试 (需要 Java 17)

**步骤 1**: 切换到 Java 17
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH
java -version  # 确认显示 17
```

**步骤 2**: 运行测试
```bash
cd admin-backend
./apache-maven-3.9.6/bin/mvn test
```

**步骤 3**: 生成覆盖率报告
```bash
./apache-maven-3.9.6/bin/mvn jacoco:report
open target/site/jacoco/index.html
```

### 执行前端测试

```bash
cd admin-web
npm install          # 首次安装依赖
npm run test         # 运行测试
npm run test:coverage  # 生成覆盖率报告
open coverage/index.html
```

---

## ⚠️ 常见问题

### Q1: Java 版本错误
**错误**: `Fatal error compiling: java.lang.ExceptionInInitializerError`

**解决**: 需要切换到 Java 17,详见 `TEST_RESULTS.md` 中的解决方案。

### Q2: Maven 未找到
**解决**: 已下载本地 Maven 3.9.6,使用相对路径:
```bash
./apache-maven-3.9.6/bin/mvn test
```

### Q3: 前端依赖安装失败
**解决**: 
```bash
npm install --legacy-peer-deps
```

---

## 📊 测试统计

| 模块 | 测试用例 | 状态 | 执行命令 |
|------|---------|------|---------|
| 后端 | 9 | ✅ 已编写 | `mvn test` |
| 前端 | 8 | ⏳ 已配置 | `npm run test` |
| **总计** | **17** | **部分完成** | - |

---

## 📁 测试文件位置

### 后端
```
admin-backend/src/test/java/com/survey/
├── SurveyApplicationTests.java
└── service/impl/
    ├── ProjectServiceImplTest.java
    └── SurveyTemplateServiceImplTest.java
```

### 前端
```
admin-web/tests/
├── setup.js
├── components/
│   ├── Login.spec.js
│   └── ProjectList.spec.js
└── api/
    └── project.spec.js
```

---

## 📝 相关文档

- `TEST_RESULTS.md` - 完整执行结果和问题解决方案
- `TEST_SUMMARY.md` - 简明摘要
- `TEST_REPORT.md` - 详细总结报告
- `admin-backend/TEST_REPORT.md` - 后端详细报告
- `admin-web/FRONTEND_TEST_REPORT.md` - 前端详细报告

---

**最后更新**: 2026-04-16

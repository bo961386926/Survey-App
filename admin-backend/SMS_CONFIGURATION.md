# 短信服务配置说明

## 概述

系统提供两种短信服务实现：

1. **Mock模式**（默认）- 用于开发环境，验证码打印到日志
2. **阿里云短信** - 用于生产环境，接入真实短信服务

## 模式切换

### 开发环境（Mock模式）

默认使用Mock模式，无需额外配置。验证码会打印到控制台日志中：

```
📱 [Mock短信服务] 发送验证码: phone=13800138000, scene=login, code=123456
⚠️  提示：这是Mock实现，生产环境请配置 aliyun.sms.enabled=true 接入真实短信服务
```

### 生产环境（阿里云短信）

#### 1. 前置准备

1. 注册阿里云账号并实名认证
2. 开通短信服务
3. 创建短信签名（如：青泓勘察）
4. 创建短信模板（如：验证码模板）
5. 获取 AccessKey ID 和 AccessKey Secret

#### 2. 配置环境变量

```bash
# 启用阿里云短信
export ALIYUN_SMS_ENABLED=true

# 配置AccessKey
export ALIYUN_SMS_ACCESS_KEY_ID=your-access-key-id
export ALIYUN_SMS_ACCESS_KEY_SECRET=your-access-key-secret

# 配置短信签名和模板
export ALIYUN_SMS_SIGN_NAME=青泓勘察
export ALIYUN_SMS_TEMPLATE_CODE=SMS_123456789
```

#### 3. 或在 application.yml 中配置

```yaml
aliyun:
  sms:
    enabled: true
    access-key-id: your-access-key-id
    access-key-secret: your-access-key-secret
    sign-name: 青泓勘察
    template-code: SMS_123456789
    endpoint: dysmsapi.aliyuncs.com
```

#### 4. 短信模板格式

在阿里云创建模板时，请使用以下格式：

```
您的验证码为：${code}，该验证码5分钟内有效，请勿泄漏于他人。
```

模板变量：
- `${code}` - 6位数字验证码

## 实现类说明

### Mock实现（开发环境）
- **类名：** `SmsCodeServiceImpl`
- **条件：** `aliyun.sms.enabled=false` 或未配置
- **特点：** 验证码打印到日志，适合开发测试

### 阿里云实现（生产环境）
- **类名：** `AliyunSmsCodeServiceImpl`
- **条件：** `aliyun.sms.enabled=true`
- **特点：** 调用阿里云短信API，适合生产环境

## 功能特性

两种实现都支持以下功能：

✅ 6位随机验证码生成  
✅ 5分钟验证码有效期  
✅ 60秒发送间隔限制  
✅ Redis存储验证码  
✅ 验证码验证后自动删除  
✅ 防重复提交机制  

## 测试验证

### 测试Mock模式

```bash
# 启动服务（默认Mock模式）
mvn spring-boot:run

# 发送验证码（验证码会打印到日志）
curl -X POST http://localhost:8080/api/auth/sms-code \
  -H "Content-Type: application/json" \
  -d '{"phone": "13800138000", "scene": "login"}'

# 查看日志获取验证码
# 📱 [Mock短信服务] 发送验证码: phone=13800138000, scene=login, code=123456
```

### 测试阿里云模式

```bash
# 配置环境变量
export ALIYUN_SMS_ENABLED=true
export ALIYUN_SMS_ACCESS_KEY_ID=your-key
export ALIYUN_SMS_ACCESS_KEY_SECRET=your-secret
export ALIYUN_SMS_SIGN_NAME=青泓勘察
export ALIYUN_SMS_TEMPLATE_CODE=SMS_123456789

# 启动服务
mvn spring-boot:run

# 发送验证码（会收到真实短信）
curl -X POST http://localhost:8080/api/auth/sms-code \
  -H "Content-Type: application/json" \
  -d '{"phone": "13800138000", "scene": "login"}'
```

## 常见问题

### Q1: 如何切换到阿里云短信？
A: 设置环境变量 `ALIYUN_SMS_ENABLED=true` 并配置相关参数即可。

### Q2: 开发环境一定要用阿里云短信吗？
A: 不需要。开发环境默认使用Mock模式，验证码直接打印到日志，更方便测试。

### Q3: 阿里云短信费用多少？
A: 阿里云短信按条计费，约0.045元/条。具体价格请参考阿里云官网。

### Q4: 支持其他短信服务商吗？
A: 当前支持阿里云。如需支持腾讯云等，可参考 `AliyunSmsCodeServiceImpl` 的实现方式创建新的实现类。

## 安全建议

1. **生产环境务必使用真实短信服务**
2. **AccessKey 请通过环境变量配置，不要硬编码**
3. **建议设置每日发送上限，防止恶意刷短信**
4. **定期审查短信发送日志，发现异常及时处理**

---

**更新日期：** 2026-05-05  
**版本：** v1.0

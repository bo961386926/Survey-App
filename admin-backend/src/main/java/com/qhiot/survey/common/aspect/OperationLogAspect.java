package com.qhiot.survey.common.aspect;

import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.common.util.SecurityUtils;
import com.qhiot.survey.security.LoginUser;
import com.qhiot.survey.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 操作日志切面
 * 自动记录标注了 @OperationLog 注解的方法调用
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 定义切点：所有标注了 @OperationLog 注解的方法
     */
    @Pointcut("@annotation(com.qhiot.survey.common.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    /**
     * 方法成功返回后记录日志（异步）
     * 注意：在主线程中获取用户信息和请求信息，然后传递给异步方法
     * 只记录成功的操作，不记录失败的操作
     */
    @AfterReturning(pointcut = "operationLogPointcut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("========== [操作日志-AOP] 方法执行成功，准备记录日志 ==========");
        log.info("[操作日志-AOP] 类名: {}", joinPoint.getSignature().getDeclaringTypeName());
        log.info("[操作日志-AOP] 方法名: {}", joinPoint.getSignature().getName());
        
        LogContext context = buildLogContext(joinPoint, result, null);
        recordLogAsync(context, joinPoint, result, null);
    }

    /**
     * 注意：方法抛出异常时不记录操作日志
     * 操作日志只记录成功的业务操作，失败的操作不应出现在操作日志中
     * 异常信息应该由专门的错误日志系统记录
     */

    /**
     * 构建日志上下文（在主线程中调用，获取用户信息和请求信息）
     */
    private LogContext buildLogContext(JoinPoint joinPoint, Object result, Exception exception) {
        LogContext context = new LogContext();
        
        // 调试：打印 SecurityContext 状态
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("[操作日志-调试] SecurityContext 状态: {}", auth != null ? "存在" : "不存在");
        if (auth != null) {
            log.info("[操作日志-调试] Authentication 类型: {}", auth.getClass().getName());
            log.info("[操作日志-调试] Principal 类型: {}", auth.getPrincipal().getClass().getName());
            log.info("[操作日志-调试] Principal: {}", auth.getPrincipal());
            log.info("[操作日志-调试] Name: {}", auth.getName());
            log.info("[操作日志-调试] Authorities: {}", auth.getAuthorities());
            
            if (auth.getPrincipal() instanceof LoginUser loginUser) {
                log.info("[操作日志-调试] LoginUser.userId: {}", loginUser.getUserId());
                log.info("[操作日志-调试] LoginUser.username: {}", loginUser.getUsername());
                log.info("[操作日志-调试] LoginUser.realName: {}", loginUser.getRealName());
            } else {
                log.warn("[操作日志-调试] Principal 不是 LoginUser 类型!");
            }
        }
        
        context.userId = SecurityUtils.getCurrentUserId();
        context.username = SecurityUtils.getCurrentUsername();
        
        log.info("[操作日志-调试] SecurityUtils.getCurrentUserId(): {}", context.userId);
        log.info("[操作日志-调试] SecurityUtils.getCurrentUsername(): {}", context.username);
        
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            context.ip = getClientIp(request);
            context.userAgent = request.getHeader("User-Agent");
            log.info("[操作日志-调试] 请求IP: {}", context.ip);
        } else {
            context.ip = "unknown";
            context.userAgent = "unknown";
            log.warn("[操作日志-调试] 无法获取 ServletRequestAttributes");
        }
        
        return context;
    }

    /**
     * 异步记录日志
     */
    @Async("operationLogExecutor")
    public void recordLogAsync(LogContext context, JoinPoint joinPoint, Object result, Exception exception) {
        log.info("[操作日志-异步] 开始异步记录日志 - userId: {}, username: {}", context.userId, context.username);
        recordLog(context, joinPoint, result, exception);
    }

    /**
     * 记录操作日志
     * 注意：只记录成功的操作，不记录异常信息
     */
    private void recordLog(LogContext context, JoinPoint joinPoint, Object result, Exception exception) {
        try {
            log.info("---------- [操作日志-记录] 开始记录操作日志 ----------");
            
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            log.info("[操作日志-记录] 目标方法: {}.{}", signature.getDeclaringTypeName(), method.getName());
            
            OperationLog operationLog = method.getAnnotation(OperationLog.class);
            if (operationLog == null) {
                log.warn("[操作日志-记录] 未找到@OperationLog注解，跳过记录");
                return;
            }
            log.info("[操作日志-记录] 注解信息 - module: {}, action: {}, riskLevel: {}", 
                operationLog.module(), operationLog.action(), operationLog.riskLevel());

            log.info("[操作日志-记录] 当前用户 - userId: {}, username: {}", context.userId, context.username);
            
            if (context.userId == null || "system".equals(context.username)) {
                log.warn("[操作日志-记录] 无法获取当前用户信息 (userId={}, username={})，跳过操作日志记录", context.userId, context.username);
                return;
            }

            log.info("[操作日志-记录] 请求信息 - IP: {}, UserAgent: {}", context.ip, context.userAgent);

            // 解析描述信息
            String description = parseDescription(operationLog.description(), joinPoint, result);
            
            if (description == null || description.isEmpty()) {
                description = operationLog.action();
            }
            
            log.info("[操作日志-记录] 准备调用Service保存 - userId: {}, username: {}, module: {}, action: {}, description: {}, riskLevel: {}",
                context.userId, context.username, operationLog.module(), operationLog.action(), description, operationLog.riskLevel());

            operationLogService.logOperation(
                context.userId,
                context.username,
                operationLog.module(),
                operationLog.action(),
                description,
                context.ip,
                context.userAgent,
                operationLog.riskLevel()
            );
            
            log.info("========== [操作日志-记录] 操作日志记录成功 ==========\n");
                
        } catch (Exception e) {
            log.error("========== [操作日志-记录] 记录操作日志失败 ==========", e);
        }
    }

    /**
     * 解析SpEL模板表达式
     * 支持类似 "创建模板: #template.templateName" 这样的模板语法
     */
    private String parseDescription(String expression, JoinPoint joinPoint, Object result) {
        if (expression == null || expression.isEmpty()) {
            return "";
        }

        try {
            if (!expression.contains("#")) {
                return expression;
            }

            EvaluationContext spelContext = new StandardEvaluationContext();

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] parameterNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            if (parameterNames != null && args != null) {
                for (int i = 0; i < parameterNames.length; i++) {
                    spelContext.setVariable(parameterNames[i], args[i]);
                }
            }

            spelContext.setVariable("result", result);

            // 将 #variable 形式的引用转换为 #{#variable} 模板语法
            // 示例:
            //   "创建模板: #template.templateName" -> "创建模板: #{#template.templateName}"
            //   "数量: #ids.size()"                -> "数量: #{#ids.size()}"
            String templateExpr = expression.replaceAll("#(\\w[\\w.]*(?:\\([^)]*\\))?)", "#{#$1}");

            // 使用模板解析器，#{...} 内的内容作为SpEL表达式，外部作为纯文本
            ParserContext templateCtx = new TemplateParserContext("#{", "}");
            return parser.parseExpression(templateExpr, templateCtx).getValue(spelContext, String.class);

        } catch (Exception e) {
            log.warn("解析操作日志描述表达式失败: {}", expression, e);
            return expression;
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }

    /**
     * 日志上下文（用于在主线程和异步线程之间传递数据）
     */
    private static class LogContext {
        Long userId;
        String username;
        String ip;
        String userAgent;
    }
}

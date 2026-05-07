package com.qhiot.survey.common.aspect;

import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.common.util.SecurityUtils;
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
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.scheduling.annotation.Async;
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
     */
    @AfterReturning(pointcut = "operationLogPointcut()", returning = "result")
    @Async("operationLogExecutor")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("========== [操作日志-AOP] 方法执行成功，准备记录日志 ==========");
        log.info("[操作日志-AOP] 类名: {}", joinPoint.getSignature().getDeclaringTypeName());
        log.info("[操作日志-AOP] 方法名: {}", joinPoint.getSignature().getName());
        recordLog(joinPoint, result, null);
    }

    /**
     * 方法抛出异常后记录日志（异步）
     */
    @AfterThrowing(pointcut = "operationLogPointcut()", throwing = "exception")
    @Async("operationLogExecutor")
    public void doAfterThrowing(JoinPoint joinPoint, Exception exception) {
        log.warn("========== [操作日志-AOP] 方法执行异常，准备记录异常日志 ==========");
        log.warn("[操作日志-AOP] 类名: {}", joinPoint.getSignature().getDeclaringTypeName());
        log.warn("[操作日志-AOP] 方法名: {}", joinPoint.getSignature().getName());
        log.warn("[操作日志-AOP] 异常信息: {}", exception.getMessage());
        recordLog(joinPoint, null, exception);
    }

    /**
     * 记录操作日志
     */
    private void recordLog(JoinPoint joinPoint, Object result, Exception exception) {
        try {
            log.info("---------- [操作日志-记录] 开始记录操作日志 ----------");
            
            // 获取方法签名
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            log.info("[操作日志-记录] 目标方法: {}.{}", signature.getDeclaringTypeName(), method.getName());
            
            // 获取注解
            OperationLog operationLog = method.getAnnotation(OperationLog.class);
            if (operationLog == null) {
                log.warn("[操作日志-记录] 未找到@OperationLog注解，跳过记录");
                return;
            }
            log.info("[操作日志-记录] 注解信息 - module: {}, action: {}, riskLevel: {}", 
                operationLog.module(), operationLog.action(), operationLog.riskLevel());

            // 获取当前用户信息
            Long userId = SecurityUtils.getCurrentUserId();
            String username = SecurityUtils.getCurrentUsername();
            log.info("[操作日志-记录] 当前用户 - userId: {}, username: {}", userId, username);
            
            // 如果获取不到用户信息，可能是未登录或系统调用
            if (userId == null || "system".equals(username)) {
                log.warn("[操作日志-记录] 无法获取当前用户信息 (userId={}, username={})，跳过操作日志记录", userId, username);
                return;
            }

            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String ip = "unknown";
            String userAgent = "unknown";
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ip = getClientIp(request);
                userAgent = request.getHeader("User-Agent");
                log.info("[操作日志-记录] 请求信息 - IP: {}, UserAgent: {}", ip, userAgent);
            } else {
                log.warn("[操作日志-记录] 无法获取ServletRequestAttributes");
            }

            // 解析操作描述（支持SpEL表达式）
            String description = parseDescription(operationLog.description(), joinPoint, result);
            
            // 如果描述为空，使用默认描述
            if (description == null || description.isEmpty()) {
                description = operationLog.action();
            }

            // 如果是异常，在描述中添加异常信息
            if (exception != null) {
                description += " [失败: " + exception.getMessage() + "]";
            }
            
            log.info("[操作日志-记录] 准备调用Service保存 - userId: {}, username: {}, module: {}, action: {}, description: {}, riskLevel: {}",
                userId, username, operationLog.module(), operationLog.action(), description, operationLog.riskLevel());

            // 记录日志
            operationLogService.logOperation(
                userId,
                username,
                operationLog.module(),
                operationLog.action(),
                description,
                ip,
                userAgent,
                operationLog.riskLevel()
            );
            
            log.info("========== [操作日志-记录] 操作日志记录成功 ==========\n");
                
        } catch (Exception e) {
            // 日志记录失败不应影响业务逻辑
            log.error("========== [操作日志-记录] 记录操作日志失败 ==========", e);
        }
    }

    /**
     * 解析SpEL表达式
     */
    private String parseDescription(String expression, JoinPoint joinPoint, Object result) {
        if (expression == null || expression.isEmpty()) {
            return "";
        }

        try {
            // 如果没有SpEL表达式，直接返回
            if (!expression.contains("#")) {
                return expression;
            }

            // 构建SpEL上下文
            EvaluationContext context = new StandardEvaluationContext();
            
            // 添加方法参数到上下文
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] parameterNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            
            if (parameterNames != null && args != null) {
                for (int i = 0; i < parameterNames.length; i++) {
                    context.setVariable(parameterNames[i], args[i]);
                }
            }
            
            // 添加返回值到上下文
            context.setVariable("result", result);

            // 解析表达式
            return parser.parseExpression(expression).getValue(context, String.class);
            
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
        
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}

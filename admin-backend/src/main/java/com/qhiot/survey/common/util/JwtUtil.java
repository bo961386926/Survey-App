package com.qhiot.survey.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * 生成访问Token
     */
    public String generateAccessToken(Long userId, String username, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tokenType", tokenType);
        return generateToken(claims, expiration);
    }

    /**
     * 生成刷新Token
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tokenType", "refresh");
        return generateToken(claims, refreshExpiration);
    }

    /**
     * 生成第三方协作访问 Token
     * 通过 loginType=collab 与内部用户 Token 区分，并携带 collabEntryId 限定访问范围
     */
    public String generateCollabToken(Long entryId, String entryName, Long ttlMillis) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", entryId);
        claims.put("username", "collab:" + entryId);
        claims.put("tokenType", "access");
        claims.put("loginType", "collab");
        claims.put("collabEntryId", entryId);
        if (entryName != null) {
            claims.put("entryName", entryName);
        }
        return generateToken(claims, ttlMillis);
    }

    /**
     * 生成Token
     */
    private String generateToken(Map<String, Object> claims, Long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从Token中获取Claims
     */
    public Claims getClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 获取Token类型
     */
    public String getTokenType(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("tokenType", String.class);
    }

    /**
     * 获取登录类型 (internal / collab)
     */
    public String getLoginType(String token) {
        Claims claims = getClaimsFromToken(token);
        Object loginType = claims.get("loginType");
        return loginType == null ? null : loginType.toString();
    }

    /**
     * 从协作Token中获取协作入口ID
     */
    public Long getCollabEntryIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object id = claims.get("collabEntryId");
        if (id == null) {
            return null;
        }
        if (id instanceof Number) {
            return ((Number) id).longValue();
        }
        try {
            return Long.valueOf(id.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
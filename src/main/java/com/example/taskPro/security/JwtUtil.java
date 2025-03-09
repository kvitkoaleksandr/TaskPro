package com.example.taskPro.security;

import com.example.taskPro.exception.UserNotFoundException;
import com.example.taskPro.model.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;
    private static final long EXPIRATION_TIME = 86400000;
    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // ✅ Генерация JWT токена
    public String generateToken(Long userId, String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Извлечение userId из токена
    public Long extractUserId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", Long.class);
    }

    // ✅ Валидация токена
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // ✅ Извлечение userId из Authentication (замена дублирующегося кода!)
    public Long getEntityIdFromAuth(Authentication authentication) {
        return validateAuthenticationAndExtractUser(authentication).getId();
    }

    // ✅ Универсальный метод проверки аутентификации и получения объекта пользователя
    private User validateAuthenticationAndExtractUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UserNotFoundException("Ошибка аутентификации: пользователь не найден!");
        }
        return (User) authentication.getPrincipal();
    }
}
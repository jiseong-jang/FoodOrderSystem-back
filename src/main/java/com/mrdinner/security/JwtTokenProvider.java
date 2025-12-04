package com.mrdinner.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT_SECRET environment variable is not set or is empty. Please set JWT_SECRET to at least 64 characters long random string.");
        }
        
        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 64) {
            throw new IllegalStateException(
                String.format("JWT_SECRET must be at least 64 bytes long (approximately 64 characters for ASCII). " +
                    "Current byte length: %d, character length: %d. " +
                    "Please generate a longer JWT_SECRET using: openssl rand -base64 64", 
                    secretBytes.length, jwtSecret.length())
            );
        }
        
        return Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateToken(String userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}


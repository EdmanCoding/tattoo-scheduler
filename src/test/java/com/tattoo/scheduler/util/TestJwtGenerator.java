package com.tattoo.scheduler.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

@Component
public class TestJwtGenerator {
        private final String secret;

        public TestJwtGenerator(@Value("${jwt.secret}") String secret) {
                this.secret = secret;
        }

        private SecretKey getKey() {
                return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }

        public String generateValidToken(String email) {
                return Jwts.builder()
                        .subject(email)
                        .issuedAt(new Date())
                        .expiration(new Date(System.currentTimeMillis() + 86_400_000)) // 24 hours
                        .signWith(getKey())
                        .compact();
        }
        public String generateExpiredToken(String email) {
                return Jwts.builder()
                        .subject(email)
                        .issuedAt(new Date(System.currentTimeMillis() - 120_000))       // 2 min ago
                        .expiration(new Date(System.currentTimeMillis() - 60_000))      // 1 min ago
                        .signWith(getKey())
                        .compact();
        }

        public String generateTokenForNonExistentUser(){
                return generateValidToken("nonexistent@email.com");
        }
        public String generateMalformedToken() {
                return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJmYWtlIn0.invalid";
        }
}

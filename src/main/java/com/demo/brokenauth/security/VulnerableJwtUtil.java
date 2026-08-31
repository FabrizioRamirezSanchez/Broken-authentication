package com.demo.brokenauth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class VulnerableJwtUtil {
    
    // VULNERABILITY: Weak secret key (though library requires minimum 256 bits)
    // FIX: Use a strong, randomly generated secret key with at least 256 bits
    private static final String SECRET_KEY = "my-weak-secret-key-123-this-is-now-long-enough-for-jwt";
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
    
    // VULNERABILITY: JWT without expiration check
    // FIX: Always set expiration time and validate it
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                // VULNERABILITY: No expiration set - tokens never expire
                // FIX: .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // VULNERABILITY: Accepts JWT with "none" algorithm (bypass signature verification)
    // FIX: Reject tokens with "none" algorithm, always verify signature
    public String generateTokenWithNoneAlgorithm(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                // VULNERABILITY: Using "none" algorithm - no signature verification
                .setHeaderParam("alg", "none")
                .compact();
    }
    
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }
    
    // VULNERABILITY: Does not validate token expiration
    // FIX: Check expiration date before accepting token
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // VULNERABILITY: Weak signature validation - accepts "none" algorithm
    // FIX: Explicitly reject "none" algorithm and enforce strong algorithms
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (UnsupportedJwtException e) {
            // VULNERABILITY: Accepts unsigned tokens with "none" algorithm
            if (token.contains("\"alg\":\"none\"")) {
                return Jwts.parserBuilder()
                        .build()
                        .parseClaimsJwt(token.substring(0, token.lastIndexOf('.') + 1))
                        .getBody();
            }
            throw e;
        }
    }
}

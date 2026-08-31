package com.demo.brokenauth.controller;

import com.demo.brokenauth.model.User;
import com.demo.brokenauth.repository.UserRepository;
import com.demo.brokenauth.security.VulnerableJwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserRepository userRepository;
    private final VulnerableJwtUtil jwtUtil;
    
    public AuthController(UserRepository userRepository, VulnerableJwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }
    
    // VULNERABILITY: No rate limiting on login endpoint
    // VULNERABILITY: No account lockout after failed attempts
    // VULNERABILITY: No captcha protection
    // FIX: Implement rate limiting, account lockout, and captcha
    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    // VULNERABILITY: Plain text password comparison
                    // FIX: Use BCrypt to verify hashed passwords
                    if (user.getPassword().equals(password)) {
                        String token = jwtUtil.generateToken(username);
                        Map<String, Object> response = new HashMap<>();
                        response.put("token", token);
                        response.put("username", user.getUsername());
                        return Mono.just(ResponseEntity.ok(response));
                    } else {
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("error", "Invalid credentials");
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
                    }
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found")));
    }
    
    // VULNERABILITY: Password reset without proper verification
    // VULNERABILITY: No token/email verification required
    // FIX: Implement email verification with time-limited tokens
    @PostMapping("/reset-password")
    public Mono<ResponseEntity<String>> resetPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String newPassword = request.get("newPassword");
        
        // VULNERABILITY: No password strength validation
        // FIX: Implement strong password requirements
        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    user.setPassword(newPassword); // Storing in plain text
                    return userRepository.save(user)
                            .then(Mono.just(ResponseEntity.ok("Password reset successful")));
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }
    
    // VULNERABILITY: Sensitive authentication data in URL
    // FIX: Never send tokens or credentials in URL, use headers/body
    @GetMapping("/verify-token")
    public Mono<ResponseEntity<Map<String, Object>>> verifyToken(@RequestParam String token) {
        // VULNERABILITY: Token passed in URL parameter (logged in proxies, browser history)
        Map<String, Object> response = new HashMap<>();
        if (jwtUtil.validateToken(token)) {
            response.put("valid", true);
            response.put("username", jwtUtil.extractUsername(token));
            return Mono.just(ResponseEntity.ok(response));
        } else {
            response.put("valid", false);
            return Mono.just(ResponseEntity.ok(response));
        }
    }
    
    // VULNERABILITY: Registration without email verification
    // VULNERABILITY: Allows weak passwords
    // FIX: Implement email verification and password strength checks
    @PostMapping("/register")
    public Mono<ResponseEntity<Map<String, Object>>> register(@RequestBody User user) {
        return userRepository.findByUsername(user.getUsername())
                .flatMap(existingUser -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Username already exists");
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body(errorResponse));
                })
                .switchIfEmpty(
                        userRepository.save(user)
                                .map(savedUser -> {
                                    String token = jwtUtil.generateToken(savedUser.getUsername());
                                    Map<String, Object> response = new HashMap<>();
                                    response.put("token", token);
                                    response.put("username", savedUser.getUsername());
                                    return ResponseEntity.ok(response);
                                })
                );
    }
}

package com.demo.brokenauth.controller;

import com.demo.brokenauth.model.User;
import com.demo.brokenauth.repository.UserRepository;
import com.demo.brokenauth.security.VulnerableJwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    
    private final UserRepository userRepository;
    private final VulnerableJwtUtil jwtUtil;
    
    public AccountController(UserRepository userRepository, VulnerableJwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }
    
    // VULNERABILITY: Email change without password confirmation
    // VULNERABILITY: Allows account takeover if token is stolen
    // FIX: Require current password for sensitive operations
    @PutMapping("/email")
    public Mono<ResponseEntity<String>> updateEmail(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        
        String token = authHeader.replace("Bearer ", "");
        String newEmail = request.get("email");
        
        if (!jwtUtil.validateToken(token)) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token"));
        }
        
        String username = jwtUtil.extractUsername(token);
        
        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    // VULNERABILITY: No password confirmation required
                    // FIX: Ask user to confirm with current password
                    user.setEmail(newEmail);
                    return userRepository.save(user)
                            .then(Mono.just(ResponseEntity.ok("Email updated successfully")));
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }
    
    // VULNERABILITY: Password change without requiring current password
    // FIX: Always require current password for password changes
    @PutMapping("/password")
    public Mono<ResponseEntity<String>> updatePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        
        String token = authHeader.replace("Bearer ", "");
        String newPassword = request.get("newPassword");
        
        if (!jwtUtil.validateToken(token)) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token"));
        }
        
        String username = jwtUtil.extractUsername(token);
        
        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    // VULNERABILITY: No current password verification
                    // FIX: Verify current password before allowing change
                    user.setPassword(newPassword);
                    return userRepository.save(user)
                            .then(Mono.just(ResponseEntity.ok("Password updated successfully")));
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }
    
    // VULNERABILITY: Sensitive account information without proper authorization check
    @GetMapping("/profile")
    public Mono<ResponseEntity<User>> getProfile(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.replace("Bearer ", "");
        
        if (!jwtUtil.validateToken(token)) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        
        String username = jwtUtil.extractUsername(token);
        
        return userRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}

package com.demo.brokenauth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {
    
    @Id
    private Long id;
    private String username;
    
    // VULNERABILITY: Password stored in plain text (no hashing/encryption)
    // FIX: Use BCrypt or Argon2 for password hashing
    private String password;
    
    private String email;
    private String role;
}

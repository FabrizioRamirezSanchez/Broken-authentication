package com.demo.brokenauth.graphql;

import com.demo.brokenauth.model.User;
import com.demo.brokenauth.repository.UserRepository;
import com.demo.brokenauth.security.VulnerableJwtUtil;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class UserGraphQLResolver {
    
    private final UserRepository userRepository;
    private final VulnerableJwtUtil jwtUtil;
    
    public UserGraphQLResolver(UserRepository userRepository, VulnerableJwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }
    
    @QueryMapping
    public Mono<User> getUserById(@Argument String id) {
        return userRepository.findById(Long.valueOf(id));
    }
    
    @QueryMapping
    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // VULNERABILITY: GraphQL login without rate limiting per mutation
    // Attackers can batch multiple login attempts in a single HTTP request
    // to bypass rate limiting that's applied at the HTTP level
    // FIX: Implement rate limiting at the mutation level, not just HTTP level
    @MutationMapping
    public Mono<AuthResponse> login(@Argument String username, @Argument String password) {
        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    // VULNERABILITY: Plain text password comparison
                    if (user.getPassword().equals(password)) {
                        String token = jwtUtil.generateToken(username);
                        AuthResponse response = new AuthResponse();
                        response.setToken(token);
                        response.setUsername(user.getUsername());
                        return Mono.just(response);
                    } else {
                        AuthResponse response = new AuthResponse();
                        response.setToken("");
                        response.setUsername("");
                        return Mono.just(response);
                    }
                })
                .defaultIfEmpty(new AuthResponse());
    }
    
    @MutationMapping
    public Mono<User> createUser(@Argument String username, @Argument String password, @Argument String email) {
        User user = new User(null, username, password, email, "USER");
        return userRepository.save(user);
    }
}

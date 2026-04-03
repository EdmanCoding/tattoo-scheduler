package com.tattoo.scheduler.controller;

import com.tattoo.scheduler.dto.AuthResponse;
import com.tattoo.scheduler.dto.LoginRequest;
import com.tattoo.scheduler.model.UserEntity;
import com.tattoo.scheduler.repository.UserRepository;
import com.tattoo.scheduler.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
        // 1. Find user by email
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        // 2. Check password (BCrypt)
        if(!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new UsernameNotFoundException("Invalid credentials");
        }
        // 3. Generate JWT
        String token = jwtUtil.generateToken(user.getEmail());

        // 4. Return token
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail()));
    }
}

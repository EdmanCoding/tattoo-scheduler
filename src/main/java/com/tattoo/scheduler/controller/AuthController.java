package com.tattoo.scheduler.controller;

import com.tattoo.scheduler.domain.User;
import com.tattoo.scheduler.dto.auth.AuthResponse;
import com.tattoo.scheduler.dto.auth.LoginRequest;
import com.tattoo.scheduler.dto.auth.RegisterRequest;
import com.tattoo.scheduler.dto.auth.RegisterResponse;
import com.tattoo.scheduler.service.UserService;
import com.tattoo.scheduler.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.authenticate(request.email(), request.password());
        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail()));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerUser(request);
        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse(token, user.getId(), user.getEmail(), user.getName()));
    }
}

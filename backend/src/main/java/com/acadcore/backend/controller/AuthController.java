package com.acadcore.backend.controller;

import com.acadcore.backend.dto.LoginRequest;
import com.acadcore.backend.dto.LoginResponse;
import com.acadcore.backend.entity.Role;
import com.acadcore.backend.entity.User;
import com.acadcore.backend.repository.UserRepository;
import com.acadcore.backend.service.CustomUserDetailsService;
import com.acadcore.backend.service.JwtService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");
        String password = request.get("password");
        String fullName = request.get("fullName");

        if (email == null ||
                email.isBlank() ||
                password == null ||
                password.isBlank() ||
                fullName == null ||
                fullName.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Name, email and password are required"
                            )
                    );
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Email already registered"
                            )
                    );
        }

        /*
         * Public registration can only create STUDENT accounts.
         * Admin creates Faculty accounts through /api/faculty.
         */
        User user =
                new User(
                        email,
                        passwordEncoder.encode(password),
                        fullName,
                        Role.STUDENT
                );

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Student registered successfully",
                        "email",
                        user.getEmail(),
                        "role",
                        user.getRole().name()
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(
                        request.getEmail()
                );

        String token =
                jwtService.generateToken(userDetails);

        User user =
                userRepository.findByEmail(
                        request.getEmail()
                ).orElseThrow();

        LoginResponse response =
                new LoginResponse(
                        token,
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole().name()
                );

        return ResponseEntity.ok(response);
    }
}
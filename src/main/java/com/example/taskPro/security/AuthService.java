package com.example.taskPro.security;

import com.example.taskPro.model.Role;
import com.example.taskPro.model.User;
import com.example.taskPro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String register(String email, String password, String role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email уже используется!");
        }

        Role userRole;
        try {
            userRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Некорректная роль: " + role);
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(userRole)
                .build();

        userRepository.save(user);
        return jwtUtil.generateToken(user.getId(), user.getEmail());
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Неверный email или пароль!"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Неверный email или пароль!");
        }

        return jwtUtil.generateToken(user.getId(), user.getEmail());
    }
}
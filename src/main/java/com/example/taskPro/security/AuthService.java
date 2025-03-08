package com.example.taskPro.security;

import com.example.taskPro.exception.UserNotFoundException;
import com.example.taskPro.model.Role;
import com.example.taskPro.model.User;
import com.example.taskPro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String register(String email, String password, String role) {
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Попытка регистрации с уже существующим email: {}", email);
            throw new RuntimeException("Email уже используется!");
        }

        Role userRole;
        try {
            userRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Некорректная роль при регистрации: {}", role);
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
                .orElseThrow(() -> {
                    log.warn("Неудачная попытка входа: email {} не найден", email);
                    return new UserNotFoundException("Неверный email или пароль!");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Неудачная попытка входа: неправильный пароль для email {}", email);
            throw new BadCredentialsException("Неверный email или пароль!");
        }

        return jwtUtil.generateToken(user.getId(), user.getEmail());
    }
}
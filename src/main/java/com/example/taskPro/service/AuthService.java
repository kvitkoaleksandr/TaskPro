package com.example.taskPro.service;

import com.example.taskPro.exception.InvalidUserRoleException;
import com.example.taskPro.exception.UserAlreadyExistsException;
import com.example.taskPro.exception.UserNotFoundException;
import com.example.taskPro.model.Role;
import com.example.taskPro.model.User;
import com.example.taskPro.repository.UserRepository;
import com.example.taskPro.security.JwtUtil;
import com.example.taskPro.service.interfaces.AuthServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.taskPro.dto.securityDto.AuthRequestDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements AuthServiceInterface {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public String register(AuthRequestDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Попытка регистрации с уже существующим email: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email уже используется!");
        }

        Role userRole;
        try {
            userRole = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Некорректная роль при регистрации: {}", request.getRole());
            throw new InvalidUserRoleException("Некорректная роль: " + request.getRole());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .build();

        userRepository.save(user);
        return jwtUtil.generateToken(user.getId(), user.getEmail());
    }

    @Override
    public String login(AuthRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Неудачная попытка входа: email {} не найден", request.getEmail());
                    return new UserNotFoundException("Неверный email или пароль!");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Неудачная попытка входа: неправильный пароль для email {}", request.getEmail());
            throw new BadCredentialsException("Неверный email или пароль!");
        }

        return jwtUtil.generateToken(user.getId(), user.getEmail());
    }
}
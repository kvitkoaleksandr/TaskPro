package com.example.taskPro.service.interfaces;

import com.example.taskPro.dto.securityDto.AuthRequestDto;

public interface AuthServiceInterface {
    String register(AuthRequestDto request);

    String login(AuthRequestDto request);
}
package com.example.taskPro.dto.securityDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequestDto {
    private String email;
    private String password;
    private String role;
}

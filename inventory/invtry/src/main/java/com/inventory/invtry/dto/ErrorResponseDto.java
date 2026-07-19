package com.inventory.invtry.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ErrorResponseDto(
        String apiPath,
        HttpStatus errorCode,
        String errorMsg,
        LocalDateTime errorTime
) {}

package com.account.acc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mirrors the subset of the Authentication service's user representation
 * that the Account service needs when validating a userId.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthUserResponseDto(
        Long id,
        String name,
        String email
) {}

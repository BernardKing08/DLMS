package com.dlms.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors the Authentication service's UserResponseDto - also what the
 * login endpoint now returns, since the frontend needs to know *who* just
 * logged in to know which account/profile to show.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private String role;
}

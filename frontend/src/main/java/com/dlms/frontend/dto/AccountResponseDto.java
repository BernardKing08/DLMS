package com.dlms.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Mirrors the Account service's AccountResponseDto. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDto {
    private Long accountId;
    private Long userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private boolean active;
}

package com.account.acc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
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
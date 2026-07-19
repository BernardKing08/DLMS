package com.account.acc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountCreateRequestDto {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String address;
}

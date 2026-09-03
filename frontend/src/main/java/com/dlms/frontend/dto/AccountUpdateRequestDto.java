package com.dlms.frontend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountUpdateRequestDto {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String phoneNumber;

    private String address;
}

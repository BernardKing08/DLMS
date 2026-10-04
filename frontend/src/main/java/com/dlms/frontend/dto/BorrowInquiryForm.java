package com.dlms.frontend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BorrowInquiryForm {

    @NotBlank(message = "Your name is required")
    private String name;

    @NotBlank(message = "Your email address is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "A pickup location is required")
    private String pickupLocation;

    private String notes;
}

package com.inventory.invtry.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRequestDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "At least one book must be requested")
    private List<Long> bookIds;

    @NotNull(message = "Pickup location is required")
    private String pickupLocation;

    private String notes;
}

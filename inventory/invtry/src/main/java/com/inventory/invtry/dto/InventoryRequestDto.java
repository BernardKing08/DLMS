package com.inventory.invtry.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryRequestDto {

    @NotNull
    private Long bookId;

    @Min(0)
    private int totalCopies;
}

package com.dlms.frontend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryUpdateRequestDto {

    @NotNull
    private Long bookId;

    @Min(0)
    private int totalCopies;
}

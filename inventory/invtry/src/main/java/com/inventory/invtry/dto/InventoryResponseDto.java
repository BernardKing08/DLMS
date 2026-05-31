package com.inventory.invtry.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryResponseDto {

    private Long id;
    private Long bookId;
    private int totalCopies;
    private int availableCopies;
    private boolean active;
}

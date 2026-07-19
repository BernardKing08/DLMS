package com.catalog.ctlog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors the Inventory service's response, used to enrich a book with its
 * current stock levels.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryResponseDto {

    private Long id;
    private Long bookId;
    private int totalCopies;
    private int availableCopies;
    private boolean active;
}

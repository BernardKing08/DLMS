package com.catalog.ctlog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Payload sent to the Inventory service to create the stock record for a
 * newly catalogued book.
 */
@Data
@AllArgsConstructor
public class InventoryRequestDto {

    private Long bookId;
    private int totalCopies;
}

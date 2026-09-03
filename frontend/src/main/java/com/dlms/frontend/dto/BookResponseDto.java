package com.dlms.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors the Catalog service's BookResponseDto. Catalog already enriches
 * this with live totalCopies/availableCopies pulled from Inventory, so the
 * frontend doesn't need its own separate Inventory call - one request to
 * Catalog is enough to render both the book and its availability.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookResponseDto {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private String description;
    private String coverImageUrl;
    private boolean active;
    private int totalCopies;
    private int availableCopies;
}

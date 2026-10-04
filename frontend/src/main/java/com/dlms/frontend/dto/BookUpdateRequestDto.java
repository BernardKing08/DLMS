package com.dlms.frontend.dto;

import lombok.Data;

/**
 * Mirrors the Catalog service's BookUpdateRequestDto.
 * Used by the frontend to send PATCH requests to update book metadata
 * or toggle the active flag via CatalogClient.
 */
@Data
public class BookUpdateRequestDto {

    private String title;
    private String author;
    private String isbn;
    private String category;
    private String description;
    private String coverImageUrl;
    private boolean active = true;
}

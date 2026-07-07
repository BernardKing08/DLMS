package com.inventory.invtry.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mirrors the subset of the Catalog service's book representation that the
 * Inventory service needs when validating a bookId.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogBookResponseDto(
        Long id,
        String title,
        boolean active
) {}

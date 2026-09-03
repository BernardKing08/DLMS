package com.dlms.frontend.controller;

/** View-model joining a Catalog book with its Inventory stock, for the admin inventory page. */
public record AdminInventoryRow(
        Long bookId,
        String title,
        String author,
        String category,
        int totalCopies,
        int availableCopies
) {}

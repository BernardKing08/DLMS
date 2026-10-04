package com.catalog.ctlog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Payload for updating an existing book entry.
 * All fields are settable; the caller must supply at least the required ones.
 * The {@code active} flag can be used to deactivate (remove from public view)
 * or reactivate a title without deleting it.
 */
@Data
public class BookUpdateRequestDto {

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @NotBlank
    private String isbn;

    private String category;

    private String description;

    private String coverImageUrl;

    /** Whether the book should be publicly visible and borrowable. */
    private boolean active = true;
}

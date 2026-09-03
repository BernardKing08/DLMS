package com.catalog.ctlog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookRequestDto {

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @NotBlank
    private String isbn;

    private String category;

    private String description;

    /** Optional - path or URL to a cover image. Falls back to a generic placeholder if blank. */
    private String coverImageUrl;

    @Min(0)
    private int totalCopies;
}

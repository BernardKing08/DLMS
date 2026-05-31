package com.catalog.ctlog.dto;

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
}

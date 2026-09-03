package com.catalog.ctlog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
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

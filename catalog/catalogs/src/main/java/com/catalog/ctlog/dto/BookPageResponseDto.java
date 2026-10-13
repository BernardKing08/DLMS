package com.catalog.ctlog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BookPageResponseDto {

    private List<BookResponseDto> books;
    private List<String> categories;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}

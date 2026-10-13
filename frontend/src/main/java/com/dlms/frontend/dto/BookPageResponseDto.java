package com.dlms.frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookPageResponseDto {

    private List<BookResponseDto> books;
    private List<String> categories;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}

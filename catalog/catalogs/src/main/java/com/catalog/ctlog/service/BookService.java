package com.catalog.ctlog.service;

import com.catalog.ctlog.dto.BookRequestDto;
import com.catalog.ctlog.dto.BookResponseDto;

import java.util.List;

public interface BookService {

    BookResponseDto createBook(BookRequestDto request);

    BookResponseDto getBookById(Long id);

    List<BookResponseDto> getAllBooks();
}

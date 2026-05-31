package com.catalog.ctlog.service.impl;

import com.catalog.ctlog.dto.BookRequestDto;
import com.catalog.ctlog.dto.BookResponseDto;
import com.catalog.ctlog.exception.ResourceNotFoundException;
import com.catalog.ctlog.modal.Book;
import com.catalog.ctlog.repository.BookRepository;
import com.catalog.ctlog.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public BookResponseDto createBook(BookRequestDto request) {

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .category(request.getCategory())
                .description(request.getDescription())
                .build();

        return mapToDto(bookRepository.save(book));
    }

    @Override
    public BookResponseDto getBookById(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Book", "id", id));

        return mapToDto(book);
    }

    @Override
    public List<BookResponseDto> getAllBooks() {

        return bookRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private BookResponseDto mapToDto(Book book) {
        return new BookResponseDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getCategory(),
                book.getDescription(),
                book.isActive()
        );
    }
}

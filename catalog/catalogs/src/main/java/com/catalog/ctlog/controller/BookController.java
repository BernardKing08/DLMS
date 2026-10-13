package com.catalog.ctlog.controller;

import com.catalog.ctlog.dto.BookRequestDto;
import com.catalog.ctlog.dto.BookPageResponseDto;
import com.catalog.ctlog.dto.BookResponseDto;
import com.catalog.ctlog.dto.BookUpdateRequestDto;
import com.catalog.ctlog.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookResponseDto> createBook(
            @Valid @RequestBody BookRequestDto request) {
        return new ResponseEntity<>(bookService.createBook(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getBook(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @GetMapping
    public ResponseEntity<List<BookResponseDto>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/page")
    public ResponseEntity<BookPageResponseDto> getBooksPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "title,asc") String sort,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(bookService.getBooksPage(page, size, sort, category));
    }

    /**
     * Updates an existing book's metadata and/or active status.
     * The {@code active} flag can be used to deactivate a title so it
     * no longer appears as borrowable without deleting it from the catalog.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<BookResponseDto> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequestDto request) {
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }
}

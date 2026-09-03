package com.catalog.ctlog.service.impl;

import com.catalog.ctlog.client.InventoryClient;
import com.catalog.ctlog.dto.BookRequestDto;
import com.catalog.ctlog.dto.BookResponseDto;
import com.catalog.ctlog.dto.InventoryResponseDto;
import com.catalog.ctlog.exception.ResourceNotFoundException;
import com.catalog.ctlog.modal.Book;
import com.catalog.ctlog.repository.BookRepository;
import com.catalog.ctlog.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final InventoryClient inventoryClient;

    public BookServiceImpl(BookRepository bookRepository, InventoryClient inventoryClient) {
        this.bookRepository = bookRepository;
        this.inventoryClient = inventoryClient;
    }

    @Override
    public BookResponseDto createBook(BookRequestDto request) {

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .category(request.getCategory())
                .description(request.getDescription())
                .coverImageUrl(request.getCoverImageUrl())
                .build();

        Book savedBook = bookRepository.save(book);

        // Catalog interconnects with Inventory: provision the stock record
        // for the book that was just catalogued. Deferred until after this
        // transaction commits, since Inventory calls back into Catalog to
        // confirm the book exists before it will accept the stock record.
        Long bookId = savedBook.getId();
        int totalCopies = request.getTotalCopies();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                inventoryClient.createInventory(bookId, totalCopies);
            }
        });

        return mapToDto(savedBook, request.getTotalCopies(), request.getTotalCopies());
    }

    @Override
    public BookResponseDto getBookById(Long id) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Book", "id", id));

        return enrichWithInventory(book);
    }

    @Override
    public List<BookResponseDto> getAllBooks() {

        return bookRepository.findAll()
                .stream()
                .map(this::enrichWithInventory)
                .toList();
    }

    private BookResponseDto enrichWithInventory(Book book) {
        InventoryResponseDto inventory = inventoryClient
                .getInventoryByBookId(book.getId())
                .orElse(null);

        return mapToDto(
                book,
                inventory != null ? inventory.getTotalCopies() : 0,
                inventory != null ? inventory.getAvailableCopies() : 0
        );
    }

    private BookResponseDto mapToDto(Book book, int totalCopies, int availableCopies) {
        return new BookResponseDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getCategory(),
                book.getDescription(),
                book.getCoverImageUrl(),
                book.isActive(),
                totalCopies,
                availableCopies
        );
    }
}

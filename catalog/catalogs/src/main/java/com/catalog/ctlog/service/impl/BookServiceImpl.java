package com.catalog.ctlog.service.impl;

import com.catalog.ctlog.client.InventoryClient;
import com.catalog.ctlog.dto.BookPageResponseDto;
import com.catalog.ctlog.dto.BookRequestDto;
import com.catalog.ctlog.dto.BookResponseDto;
import com.catalog.ctlog.dto.BookUpdateRequestDto;
import com.catalog.ctlog.dto.InventoryResponseDto;
import com.catalog.ctlog.exception.ResourceNotFoundException;
import com.catalog.ctlog.model.Book;
import com.catalog.ctlog.repository.BookRepository;
import com.catalog.ctlog.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final InventoryClient inventoryClient;
    private static final int MAX_PAGE_SIZE = 100;

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

    @Override
    @Transactional(readOnly = true)
    public BookPageResponseDto getBooksPage(int page, int size, String sort, String category) {
        int normalizedPage = Math.max(page, 1);
        int normalizedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(normalizedPage - 1, normalizedSize, parseSort(sort));
        Page<Book> booksPage = findBooksPage(category, pageable);

        if (booksPage.getTotalPages() > 0 && normalizedPage > booksPage.getTotalPages()) {
            normalizedPage = booksPage.getTotalPages();
            pageable = PageRequest.of(normalizedPage - 1, normalizedSize, parseSort(sort));
            booksPage = findBooksPage(category, pageable);
        }

        List<Book> books = booksPage.getContent();
        List<Long> bookIds = books.stream().map(Book::getId).toList();
        Map<Long, InventoryResponseDto> inventoriesByBookId = inventoryClient.getInventoryByBookIds(bookIds)
                .stream()
                .collect(Collectors.toMap(InventoryResponseDto::getBookId, Function.identity(), (first, ignored) -> first));

        List<BookResponseDto> enrichedBooks = books.stream()
                .map(book -> {
                    InventoryResponseDto inventory = inventoriesByBookId.get(book.getId());
                    return mapToDto(
                            book,
                            inventory != null ? inventory.getTotalCopies() : 0,
                            inventory != null ? inventory.getAvailableCopies() : 0
                    );
                })
                .toList();

        return new BookPageResponseDto(
                enrichedBooks,
                bookRepository.findDistinctCategories(),
                booksPage.getTotalElements(),
                booksPage.getTotalPages(),
                normalizedPage,
                normalizedSize
        );
    }

    @Override
    public BookResponseDto updateBook(Long id, BookUpdateRequestDto request) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setDescription(request.getDescription());
        book.setCoverImageUrl(request.getCoverImageUrl());
        book.setActive(request.isActive());

        Book saved = bookRepository.save(book);
        return enrichWithInventory(saved);
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

    private Page<Book> findBooksPage(String category, Pageable pageable) {
        return category == null || category.isBlank()
                ? bookRepository.findAll(pageable)
                : bookRepository.findByCategory(category, pageable);
    }

    private Sort parseSort(String sort) {
        String[] sortParams = sort == null ? new String[0] : sort.split(",", -1);
        String field = sortParams.length > 0 ? sortParams[0].trim() : "";
        String direction = sortParams.length > 1 ? sortParams[1].trim() : "";
        String property = "author".equalsIgnoreCase(field) ? "author" : "title";
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(new Sort.Order(sortDirection, property).ignoreCase().nullsLast())
                .and(Sort.by(Sort.Order.asc("id")));
    }
}

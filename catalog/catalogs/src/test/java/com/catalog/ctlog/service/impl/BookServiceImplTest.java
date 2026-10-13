package com.catalog.ctlog.service.impl;

import com.catalog.ctlog.client.InventoryClient;
import com.catalog.ctlog.dto.BookPageResponseDto;
import com.catalog.ctlog.dto.InventoryResponseDto;
import com.catalog.ctlog.model.Book;
import com.catalog.ctlog.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class BookServiceImplTest {

    private final BookRepository bookRepository = mock(BookRepository.class);
    private final InventoryClient inventoryClient = mock(InventoryClient.class);
    private final BookServiceImpl bookService = new BookServiceImpl(bookRepository, inventoryClient);

    @Test
    void getsOnlyRequestedPageAndEnrichesItWithOneInventoryBatch() {
        Book firstBook = book(12L, "Alpha");
        Book secondBook = book(13L, "Beta");
        when(bookRepository.findByCategory(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(firstBook, secondBook), PageRequest.of(1, 2), 5));
        when(bookRepository.findDistinctCategories()).thenReturn(List.of("Fantasy", "History"));
        when(inventoryClient.getInventoryByBookIds(List.of(12L, 13L))).thenReturn(List.of(
                new InventoryResponseDto(1L, 12L, 4, 2, true)
        ));

        BookPageResponseDto result = bookService.getBooksPage(2, 2, "title,asc", "Fantasy");

        assertEquals(2, result.getCurrentPage());
        assertEquals(3, result.getTotalPages());
        assertEquals(5, result.getTotalElements());
        assertEquals(List.of("Fantasy", "History"), result.getCategories());
        assertEquals(2, result.getBooks().size());
        assertEquals(4, result.getBooks().get(0).getTotalCopies());
        assertEquals(2, result.getBooks().get(0).getAvailableCopies());
        assertEquals(0, result.getBooks().get(1).getTotalCopies());

        verify(bookRepository).findByCategory(
                org.mockito.ArgumentMatchers.eq("Fantasy"),
                argThat(pageable -> {
                    var titleOrder = pageable.getSort().getOrderFor("title");
                    return pageable.getPageNumber() == 1
                            && pageable.getPageSize() == 2
                            && titleOrder != null
                            && titleOrder.isIgnoreCase()
                            && titleOrder.getDirection().isAscending();
                })
        );
        verify(inventoryClient).getInventoryByBookIds(List.of(12L, 13L));
    }

    @Test
    void clampsPageSizeAndOutOfRangePageToTheLastPage() {
        Book onlyBook = book(12L, "Alpha");
        when(bookRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(onlyBook), PageRequest.of(0, 100), 1));
        when(bookRepository.findDistinctCategories()).thenReturn(List.of());
        when(inventoryClient.getInventoryByBookIds(List.of(12L))).thenReturn(List.of());

        BookPageResponseDto result = bookService.getBooksPage(9, 200, "nonsense,desc", null);

        assertEquals(1, result.getCurrentPage());
        assertEquals(100, result.getPageSize());
        assertEquals(1, result.getTotalPages());
        verify(bookRepository).findAll(argThat((Pageable pageable) ->
                pageable.getPageNumber() == 0 && pageable.getPageSize() == 100));
    }

    private Book book(Long id, String title) {
        return Book.builder()
                .id(id)
                .title(title)
                .author("Author")
                .isbn("isbn-" + id)
                .category("Fantasy")
                .active(true)
                .build();
    }
}

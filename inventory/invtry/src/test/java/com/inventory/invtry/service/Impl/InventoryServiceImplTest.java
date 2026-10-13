package com.inventory.invtry.service.Impl;

import com.inventory.invtry.client.CatalogClient;
import com.inventory.invtry.dto.InventoryResponseDto;
import com.inventory.invtry.repository.BorrowRecordRepository;
import com.inventory.invtry.repository.InventoryRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InventoryServiceImplTest {

    private final InventoryRepository inventoryRepository = mock(InventoryRepository.class);
    private final BorrowRecordRepository borrowRecordRepository = mock(BorrowRecordRepository.class);
    private final CatalogClient catalogClient = mock(CatalogClient.class);
    private final InventoryServiceImpl inventoryService = new InventoryServiceImpl(
            inventoryRepository, borrowRecordRepository, catalogClient);

    @Test
    void returnsInventoryForRequestedBookIdsInOneRepositoryLookup() {
        var inventory = com.inventory.invtry.model.Inventory.builder()
                .id(4L)
                .bookId(12L)
                .totalCopies(5)
                .availableCopies(3)
                .active(true)
                .build();
        when(inventoryRepository.findByBookIdIn(List.of(12L, 13L))).thenReturn(List.of(inventory));

        List<InventoryResponseDto> result = inventoryService.getInventoryByBookIds(List.of(12L, 13L));

        assertEquals(1, result.size());
        assertEquals(12L, result.get(0).getBookId());
        assertEquals(5, result.get(0).getTotalCopies());
        assertEquals(3, result.get(0).getAvailableCopies());
        verify(inventoryRepository).findByBookIdIn(List.of(12L, 13L));
    }
}

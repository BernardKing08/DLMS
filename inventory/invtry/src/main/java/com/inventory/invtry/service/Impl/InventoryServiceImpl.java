package com.inventory.invtry.service.Impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.invtry.client.CatalogClient;
import com.inventory.invtry.dto.InventoryRequestDto;
import com.inventory.invtry.dto.InventoryResponseDto;
import com.inventory.invtry.exception.ResourceNotFoundException;
import com.inventory.invtry.modal.Inventory;
import com.inventory.invtry.repository.InventoryRepository;
import com.inventory.invtry.service.InventoryService;

import java.util.List;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final CatalogClient catalogClient;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, CatalogClient catalogClient) {
        this.inventoryRepository = inventoryRepository;
        this.catalogClient = catalogClient;
    }

    @Override
    public InventoryResponseDto createOrUpdateInventory(InventoryRequestDto request) {

        // Inventory checks the Catalog service: stock can only exist for a
        // bookId that is genuinely catalogued.
        catalogClient.getBookById(request.getBookId());

        Inventory inventory = inventoryRepository.findByBookId(request.getBookId())
                .orElse(
                        Inventory.builder()
                                .bookId(request.getBookId())
                                .build()
                );

        inventory.setTotalCopies(request.getTotalCopies());
        inventory.setAvailableCopies(request.getTotalCopies());

        Inventory saved = inventoryRepository.save(inventory);
        return mapToDto(saved);
    }

    @Override
    public InventoryResponseDto getInventoryByBookId(Long bookId) {

        Inventory inventory = inventoryRepository.findByBookId(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventory", "bookId", bookId));

        return mapToDto(inventory);
    }

    @Override
    public List<InventoryResponseDto> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    private InventoryResponseDto mapToDto(Inventory inventory) {
        return new InventoryResponseDto(
                inventory.getId(),
                inventory.getBookId(),
                inventory.getTotalCopies(),
                inventory.getAvailableCopies(),
                inventory.isActive()
        );
    }
}

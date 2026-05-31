package com.inventory.invtry.service.Impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.invtry.dto.InventoryRequestDto;
import com.inventory.invtry.dto.InventoryResponseDto;
import com.inventory.invtry.exception.ResourceNotFoundException;
import com.inventory.invtry.modal.Inventory;
import com.inventory.invtry.repository.InventoryRepository;
import com.inventory.invtry.service.InventoryService;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public InventoryResponseDto createOrUpdateInventory(InventoryRequestDto request) {

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

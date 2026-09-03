package com.inventory.invtry.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.inventory.invtry.dto.InventoryRequestDto;
import com.inventory.invtry.service.InventoryService;

import jakarta.validation.Valid;
import com.inventory.invtry.dto.InventoryResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<InventoryResponseDto> createInventory(
            @Valid @RequestBody InventoryRequestDto request) {

        return new ResponseEntity<>(
                inventoryService.createOrUpdateInventory(request),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<InventoryResponseDto> getInventory(@PathVariable Long bookId) {
        return ResponseEntity.ok(inventoryService.getInventoryByBookId(bookId));
    }

    /** Lists every stock record - general-purpose, not tied to one specific caller. */
    @GetMapping
    public ResponseEntity<List<InventoryResponseDto>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }
}

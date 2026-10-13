package com.inventory.invtry.controller;

import com.inventory.invtry.dto.BorrowRequestDto;
import com.inventory.invtry.dto.BorrowResponseDto;
import com.inventory.invtry.dto.InventoryRequestDto;
import com.inventory.invtry.dto.InventoryResponseDto;
import com.inventory.invtry.exception.ForbiddenException;
import com.inventory.invtry.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/batch")
    public ResponseEntity<List<InventoryResponseDto>> getInventoryByBookIds(
            @Valid @RequestBody @NotNull @Size(max = 100) List<@NotNull Long> bookIds) {
        return ResponseEntity.ok(inventoryService.getInventoryByBookIds(bookIds));
    }

    /** Lists every stock record - general-purpose, not tied to one specific caller. */
    @GetMapping
    public ResponseEntity<List<InventoryResponseDto>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @PostMapping("/borrow")
    public ResponseEntity<List<BorrowResponseDto>> borrowBooks(
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
            @Valid @RequestBody BorrowRequestDto request) {
        if (currentUserId == null) {
            throw new ForbiddenException("Authentication required to borrow books.");
        }
        return new ResponseEntity<>(inventoryService.borrowBooksForUser(currentUserId, request), HttpStatus.CREATED);
    }

    @GetMapping("/loans/user/{userId}")
    public ResponseEntity<List<BorrowResponseDto>> getLoansByUser(
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
            @PathVariable Long userId) {
        if (currentUserId == null) {
            throw new ForbiddenException("Authentication required to view borrow history.");
        }
        return ResponseEntity.ok(inventoryService.getLoansForUser(currentUserId, userId));
    }

    @PostMapping("/loans/{id}/return")
    public ResponseEntity<BorrowResponseDto> returnBook(
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId,
            @PathVariable Long id) {
        if (currentUserId == null) {
            throw new ForbiddenException("Authentication required to return a borrowed book.");
        }
        return ResponseEntity.ok(inventoryService.returnBookForUser(currentUserId, id));
    }
}

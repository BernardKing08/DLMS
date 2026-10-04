package com.inventory.invtry.service.Impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.invtry.client.CatalogClient;

import com.inventory.invtry.dto.BorrowRequestDto;
import com.inventory.invtry.dto.BorrowResponseDto;
import com.inventory.invtry.dto.CatalogBookResponseDto;
import com.inventory.invtry.dto.InventoryRequestDto;
import com.inventory.invtry.dto.InventoryResponseDto;
import com.inventory.invtry.exception.ResourceNotFoundException;
import com.inventory.invtry.exception.ValidationException;
import com.inventory.invtry.modal.BorrowRecord;
import com.inventory.invtry.modal.Inventory;
import com.inventory.invtry.repository.BorrowRecordRepository;
import com.inventory.invtry.repository.InventoryRepository;
import com.inventory.invtry.service.InventoryService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final CatalogClient catalogClient;

    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                BorrowRecordRepository borrowRecordRepository,
                                CatalogClient catalogClient) {
        this.inventoryRepository = inventoryRepository;
        this.borrowRecordRepository = borrowRecordRepository;
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

    @Override
    public List<BorrowResponseDto> borrowBooks(BorrowRequestDto request) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = now.plusDays(14);
        List<BorrowRecord> savedRecords = new ArrayList<>();

        for (Long bookId : request.getBookIds()) {
            // Confirm the book is active and catalogued
            CatalogBookResponseDto book = catalogClient.getBookById(bookId);
            if (!book.active()) {
                throw new ValidationException("Book '" + book.title() + "' is not currently active for borrowing.");
            }

            // Find stock record
            Inventory inventory = inventoryRepository.findByBookId(bookId)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory", "bookId", bookId));

            if (inventory.getAvailableCopies() < 1) {
                throw new ValidationException("No copies currently available to borrow for '" + book.title() + "'.");
            }

            // Decrement available copies
            inventory.setAvailableCopies(inventory.getAvailableCopies() - 1);
            inventoryRepository.save(inventory);

            // Create borrow record
            BorrowRecord record = BorrowRecord.builder()
                    .userId(request.getUserId())
                    .bookId(bookId)
                    .pickupLocation(request.getPickupLocation())
                    .notes(request.getNotes())
                    .status("BORROWED")
                    .borrowDate(now)
                    .dueDate(dueDate)
                    .build();

            savedRecords.add(borrowRecordRepository.save(record));
        }

        return savedRecords.stream().map(this::mapToBorrowDto).toList();
    }

    @Override
    public List<BorrowResponseDto> getLoansByUserId(Long userId) {
        return borrowRecordRepository.findByUserIdOrderByBorrowDateDesc(userId)
                .stream()
                .map(this::mapToBorrowDto)
                .toList();
    }

    @Override
    public BorrowResponseDto returnBook(Long loanId) {
        BorrowRecord record = borrowRecordRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowRecord", "id", loanId));

        if ("RETURNED".equalsIgnoreCase(record.getStatus())) {
            return mapToBorrowDto(record);
        }

        record.setStatus("RETURNED");
        record.setReturnDate(LocalDateTime.now());
        BorrowRecord updatedRecord = borrowRecordRepository.save(record);

        // Restore stock
        inventoryRepository.findByBookId(record.getBookId()).ifPresent(inventory -> {
            if (inventory.getAvailableCopies() < inventory.getTotalCopies()) {
                inventory.setAvailableCopies(inventory.getAvailableCopies() + 1);
                inventoryRepository.save(inventory);
            }
        });

        return mapToBorrowDto(updatedRecord);
    }

    private BorrowResponseDto mapToBorrowDto(BorrowRecord record) {
        return BorrowResponseDto.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .bookId(record.getBookId())
                .pickupLocation(record.getPickupLocation())
                .notes(record.getNotes())
                .status(record.getStatus())
                .borrowDate(record.getBorrowDate())
                .dueDate(record.getDueDate())
                .returnDate(record.getReturnDate())
                .build();
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


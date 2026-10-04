package com.inventory.invtry.service;

import com.inventory.invtry.dto.BorrowRequestDto;
import com.inventory.invtry.dto.BorrowResponseDto;
import com.inventory.invtry.dto.InventoryRequestDto;
import com.inventory.invtry.dto.InventoryResponseDto;

import java.util.List;

public interface InventoryService {

    InventoryResponseDto createOrUpdateInventory(InventoryRequestDto request);

    InventoryResponseDto getInventoryByBookId(Long bookId);

    List<InventoryResponseDto> getAllInventory();

    List<BorrowResponseDto> borrowBooks(BorrowRequestDto request);

    List<BorrowResponseDto> getLoansByUserId(Long userId);

    BorrowResponseDto returnBook(Long loanId);
}


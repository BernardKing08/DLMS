package com.account.acc.controller;

import com.account.acc.dto.AccountResponseDto;
import com.account.acc.dto.AccountUpdateRequestDto;
import com.account.acc.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AccountResponseDto> getAccount(@PathVariable Long userId) {
        return ResponseEntity.ok(accountService.getAccountByUserId(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<AccountResponseDto> updateAccount(
            @PathVariable Long userId,
            @Valid @RequestBody AccountUpdateRequestDto request) {
        return ResponseEntity.ok(accountService.updateAccount(userId, request));
    }
}


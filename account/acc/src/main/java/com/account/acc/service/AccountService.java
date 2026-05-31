package com.account.acc.service;

import com.account.acc.dto.AccountResponseDto;
import com.account.acc.dto.AccountUpdateRequestDto;

public interface AccountService {

    AccountResponseDto getAccountByUserId(Long userId);

    AccountResponseDto updateAccount(Long userId, AccountUpdateRequestDto request);
}


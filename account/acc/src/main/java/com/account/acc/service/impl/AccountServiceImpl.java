package com.account.acc.service.impl;

import com.account.acc.dto.AccountResponseDto;
import com.account.acc.dto.AccountUpdateRequestDto;
import com.account.acc.exception.ResourceNotFoundException;
import com.account.acc.modal.Account;
import com.account.acc.repository.AccountRepository;
import com.account.acc.service.AccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public AccountResponseDto getAccountByUserId(Long userId) {

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account", "userId", userId)
                );

        return mapToDto(account);
    }

    @Override
    public AccountResponseDto updateAccount(Long userId, AccountUpdateRequestDto request) {

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account", "userId", userId)
                );

        account.setFirstName(request.getFirstName());
        account.setLastName(request.getLastName());
        account.setPhoneNumber(request.getPhoneNumber());
        account.setAddress(request.getAddress());

        Account updatedAccount = accountRepository.save(account);
        return mapToDto(updatedAccount);
    }

    private AccountResponseDto mapToDto(Account account) {
        return new AccountResponseDto(
                account.getAccountId(),
                account.getUserId(),
                account.getFirstName(),
                account.getLastName(),
                account.getPhoneNumber(),
                account.getAddress(),
                account.isActive()
        );
    }
}

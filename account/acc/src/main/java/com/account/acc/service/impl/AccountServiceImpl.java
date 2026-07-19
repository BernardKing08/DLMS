package com.account.acc.service.impl;

import com.account.acc.client.AuthClient;
import com.account.acc.constants.AccountConstants;
import com.account.acc.dto.AccountCreateRequestDto;
import com.account.acc.dto.AccountResponseDto;
import com.account.acc.dto.AccountUpdateRequestDto;
import com.account.acc.dto.AuthUserResponseDto;
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
    private final AuthClient authClient;

    public AccountServiceImpl(AccountRepository accountRepository, AuthClient authClient) {
        this.accountRepository = accountRepository;
        this.authClient = authClient;
    }

    @Override
    public AccountResponseDto createAccount(AccountCreateRequestDto request) {

        if (accountRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException(AccountConstants.ACCOUNT_ALREADY_EXISTS);
        }

        // Accounts check the Authentication service: a profile can only be
        // provisioned for a userId that genuinely exists there.
        AuthUserResponseDto authUser = authClient.getUserById(request.getUserId());

        Account account = Account.builder()
                .userId(authUser.id())
                .firstName(request.getFirstName())
                .lastName(request.getLastName() != null ? request.getLastName() : "")
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .active(true)
                .build();

        return mapToDto(accountRepository.save(account));
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

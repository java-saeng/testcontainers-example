package com.practice.testcontainer.service;

import com.practice.testcontainer.domain.Account;
import com.practice.testcontainer.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Account createAccount(String name) {
        return accountRepository.save(new Account(name));
    }

    public Account findAccount(long accountId) {
        return accountRepository.findById(accountId).get();
    }

    public long deleteAccount(long accountId) {
        accountRepository.deleteById(accountId);
        return accountId;
    }

    public Account updateAccount(long accountId, String name) {
        Account savedAccount = findAccount(accountId);

        return savedAccount.update(name);
    }
}

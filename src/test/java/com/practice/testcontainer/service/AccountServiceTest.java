package com.practice.testcontainer.service;

import com.practice.testcontainer.domain.Account;
import com.practice.testcontainer.repository.AccountRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AccountService Unit Test")
public class AccountServiceTest {

    AccountRepository accountRepository;

    AccountService accountService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        accountService = new AccountService(accountRepository);
    }

    @Test
    @DisplayName("이름이 주어질 경우 계정을 생성할 수 있다")
    void testCreateAccount() throws Exception {
        //given
        String accountName = "account1";

        //when
        Account savedAccount = accountService.createAccount("account1");

        //then
        verify(accountRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("수정될 이름과 아이디가 주어질 경우 계정을 수정할 수 있다")
    void testUpdateAccount() throws Exception {
        //given
        String updatedName = "account2";
        Account account = new Account("account1");

        //when
        when(accountRepository.findById(anyLong()))
                .thenReturn(Optional.of(account));

        Account updatedAccount = accountService.updateAccount(1L, updatedName);

        //then
        Assertions.assertEquals(updatedAccount.getName(), updatedName);
    }
}

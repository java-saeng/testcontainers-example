package com.practice.testcontainer.service;

import com.practice.testcontainer.domain.Account;
import com.practice.testcontainer.repository.AccountRepository;
import com.practice.testcontainer.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@DisplayName("AccountService integration test")
class AccountServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    AccountRepository accountRepository;

    @Test
    @DisplayName("Account 저장")
    void saveAccount() throws Exception {
        //given
        Account account = new Account("account1");

        //when
        Account savedAccount = accountRepository.save(account);

        //then
        assertThat(savedAccount.getId()).isEqualTo(account.getId());
    }

    @Test
    @DisplayName("Account 수정")
    @Sql(scripts = "/sql/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/clean-up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateAccount() throws Exception {
        //given
        List<Account> accounts = accountRepository.findAll();

        assertEquals(accounts.size(), 3);

        //when
        Account updatedAccount = accounts.get(0).update("account2");

        //then
        assertThat(updatedAccount.getName()).isEqualTo("account2");
    }

}
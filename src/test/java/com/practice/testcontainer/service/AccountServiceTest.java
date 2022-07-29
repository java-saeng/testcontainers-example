package com.practice.testcontainer.service;

import com.practice.testcontainer.domain.Account;
import com.practice.testcontainer.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("test")
@SpringBootTest
@Transactional
//@Testcontainers
class AccountServiceTest {

//    @Container
//    public static MySQLContainer mysqlContainer =
//            new MySQLContainer("mysql:8.0.28")
//                    .withDatabaseName("foo");
//
//    static {
//        mysqlContainer.start();
//    }
//
//    @DynamicPropertySource
//    static void mySqlProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
//        registry.add("spring.datasource.username", mysqlContainer::getUsername);
//        registry.add("spring.datasource.password", mysqlContainer::getPassword);
//    }

    @Autowired
    AccountRepository accountRepository;

//    @Test
//    @DisplayName("test container 실행 확인")
//    void isRunningTestContainer() throws Exception {
//        assertTrue(mysqlContainer.isRunning());
//    }

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
    void updateAccount() throws Exception {
        //given
        Account account = new Account("account1");
        Account savedAccount = accountRepository.save(account);

        //when
        savedAccount.update("account2");

        Account updatedAccount = accountRepository.findById(account.getId()).get();

        //then
        assertThat(updatedAccount.getName()).isEqualTo("account2");
    }

}
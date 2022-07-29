package com.practice.testcontainer.repository;

import com.practice.testcontainer.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}

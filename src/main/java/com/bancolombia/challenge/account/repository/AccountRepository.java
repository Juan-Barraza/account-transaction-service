package com.bancolombia.challenge.account.repository;

import com.bancolombia.challenge.account.entity.Account;
import com.bancolombia.challenge.account.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);
    boolean existsByCustomerIdAndAccountType(String customerId, AccountType accountType);
}

package com.example.banking.domain.repository;

import com.example.banking.domain.model.Account;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Optional<Account> getAccount(String accountNumber);
    List<Account> getCustomerAccounts(String customerNumber);
    Account save(Account account);
    void transferBalance(Account fromAccount, Account toAccount);
}

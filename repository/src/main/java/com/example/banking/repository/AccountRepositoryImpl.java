package com.example.banking.repository;

import com.example.banking.domain.model.Account;
import com.example.banking.domain.repository.AccountRepository;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** In-memory storage: accounts are lost when the application stops. */
// Registers this repository as a Spring bean so it can be injected into controllers. Note @Repository instead of @Service!
//
// Each synchronized method locks this AccountRepositoryImpl instance while it runs.
// Only one thread can execute any synchronized repository method at a time, which
// protects the shared HashMap and keeps the two account writes in transferBalance
// together. The lock is released automatically when the method returns or throws.
@Repository
public class AccountRepositoryImpl implements AccountRepository {

    // The account number is the map key, allowing direct account lookups without an internal ID.
    private final Map<String, Account> accounts = new HashMap<>();

    @Override
    public synchronized Optional<Account> getAccount(String accountNumber) {
        // Optional.empty() tells the service layer that no account matched the supplied number.
        return Optional.ofNullable(accounts.get(accountNumber));
    }

    @Override
    public synchronized List<Account> getCustomerAccounts(String customerNumber) {
        // Filter the stored accounts to return only those belonging to this customer.
        return accounts.values().stream()
                .filter(account -> account.getCustomerNumber().equals(customerNumber))
                .toList();
    }

    @Override
    public synchronized Account save(Account account) {
        // put() creates a new entry or replaces the existing account with the same account number.
        accounts.put(account.getAccountNumber(), account);
        return account;
    }

    @Override
    public synchronized void transferBalance(Account fromAccount, Account toAccount) {
        // Store both updated balances while holding the same lock so another repository
        // operation cannot observe the transfer after only one account has been updated.
        accounts.put(fromAccount.getAccountNumber(), fromAccount);
        accounts.put(toAccount.getAccountNumber(), toAccount);
    }
}

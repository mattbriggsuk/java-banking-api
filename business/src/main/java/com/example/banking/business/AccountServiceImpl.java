package com.example.banking.business;

import com.example.banking.domain.model.Account;
import com.example.banking.utils.ValidationUtils;
import com.example.banking.utils.AccountUtils;
import com.example.banking.utils.TransactionUtils;
import com.example.banking.domain.model.Transaction;
import com.example.banking.domain.enums.TransactionType;
import com.example.banking.domain.model.TransferResult;
import com.example.banking.domain.repository.AccountRepository;
import com.example.banking.domain.repository.CustomerRepository;
import com.example.banking.domain.service.AccountService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.NoSuchElementException;

// Registers this business service as a Spring bean so it can be injected into controllers.
@Service
public class AccountServiceImpl implements AccountService {

    //region Service Dependancies

        private final AccountRepository accountRepository;
        private final CustomerRepository customerRepository;

    //endregion

    //region Constructor

        public AccountServiceImpl(AccountRepository accountRepository, CustomerRepository customerRepository) {
            this.accountRepository = accountRepository;
            this.customerRepository = customerRepository;
        }

    //endregion

    // Open a new account
    @Override
    public synchronized Account create(String customerNumber) {

        // Ensure the Customer Number is valid.
        if (!ValidationUtils.isValidCustomerNumber(customerNumber)) {
            throw new IllegalArgumentException("Customer number must be a valid 16-digit number");
        }

        // An account can only be opened for a registered customer.
        customerRepository.getCustomer(customerNumber)
                .orElseThrow(() -> new NoSuchElementException("Customer not found"));

        // Create the Account object.
        Account account = new Account(
                AccountUtils.generateAccountNumber(),
                customerNumber,
                new BigDecimal("0.00"),
                false);

        // Save the new Account to the database.
        return accountRepository.save(account);

    }

    // Close an account
    @Override
    public synchronized Account close(String accountNumber) {

        // Get the account.
        Account account = getAccount(accountNumber);

        // Ensure the account has a zero balance.
        if (account.getBalance().signum() != 0) {
            throw new IllegalStateException("Account balance must be zero before closing");
        }

        // Set the account to closed.
        account.setClosed(true);

        // Update the database.
        return accountRepository.save(account);

    }

    // Make an account deposit.
    @Override
    public synchronized Account deposit(Transaction transaction)
    {

        // Validate the transaction.
        TransactionUtils.validateTransaction(transaction, TransactionType.DEPOSIT);
        transaction.setTransactionDate(Instant.now());
        BigDecimal amount = transaction.getAmount();

        // Get the target account.
        Account account = getAccount(transaction.getTargetAccountNumber());

        // Increment the account balance.
        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);

        // Update the account in the database.
        return accountRepository.save(account);

    }

    // Withdraw funds from an account.
    @Override
    public synchronized Account withdraw(Transaction transaction) {

        // Validate the transaction.
        TransactionUtils.validateTransaction(transaction, TransactionType.WITHDRAWAL);
        transaction.setTransactionDate(Instant.now());
        BigDecimal amount = transaction.getAmount();

        // Get the target account.
        Account account = getAccount(transaction.getTargetAccountNumber());
        TransactionUtils.requireFunds(account, amount);

        // Decrement the account balance.
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);

        // Update the account in the database.
        return accountRepository.save(account);

    }

    // Transfer funds between accounts.
    // synchronized shares this instance's lock with the other synchronized account methods,
    // preventing overlapping balance updates here. It does not provide database transactions.
    @Override
    public synchronized TransferResult transfer(Transaction transaction)
    {

        // Ensure the transaction is a valid instruction.
        TransactionUtils.validateTransaction(transaction, TransactionType.TRANSFER);
        transaction.setTransactionDate(Instant.now());
        BigDecimal amount = transaction.getAmount();

        // Both accounts must exist and be open; the source must have sufficient funds.
        Account from = getAccount(transaction.getSourceAccountNumber());
        Account to = getAccount(transaction.getTargetAccountNumber());
        TransactionUtils.requireFunds(from, amount);

        // After all checks pass, update both Account objects.
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        // Transfer the balance within the database.
        accountRepository.transferBalance(from, to);

        // Return the transfer result.
        return new TransferResult(from, to);

    }

    //region Helpers

        // Get an Open Account
        private Account getAccount(String accountNumber) {
            return getAccount(accountNumber, true);
        }

        // Get the Account
        private Account getAccount(String accountNumber, boolean mustBeOpen) {

            if (!ValidationUtils.isValidAccountNumber(accountNumber)) {
                throw new IllegalArgumentException("Account number must be valid");
            }

            Account account = accountRepository.getAccount(accountNumber)
                    .orElseThrow(() -> new NoSuchElementException("Account not found"));

            if (mustBeOpen && account.isClosed()) {
                throw new IllegalStateException("Account is closed");
            }

            return account;
        }

    //endregion

}

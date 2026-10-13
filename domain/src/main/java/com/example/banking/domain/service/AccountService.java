package com.example.banking.domain.service;

import com.example.banking.domain.model.Account;
import com.example.banking.domain.model.Transaction;
import com.example.banking.domain.model.TransferResult;

public interface AccountService {
    Account create(String customerNumber);
    Account close(String accountNumber);
    Account deposit(Transaction transaction);
    Account withdraw(Transaction transaction);
    TransferResult transfer(Transaction transaction);
}

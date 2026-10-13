package com.example.banking.domain.dto;

import com.example.banking.domain.model.Account;

import java.math.BigDecimal;

public record AccountDto(String accountNumber, String customerNumber, BigDecimal balance, boolean closed) {

    /** Creates an Protected API DTO without exposing the domain Account object directly. */
    public static AccountDto fromDomain(Account account) {
        return new AccountDto(
                account.getAccountNumber(),
                account.getCustomerNumber(),
                account.getBalance(),
                account.isClosed());
    }
}

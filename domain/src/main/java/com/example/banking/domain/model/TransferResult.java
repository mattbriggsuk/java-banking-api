package com.example.banking.domain.model;

/** The account states after a successful transfer. */
public record TransferResult(Account fromAccount, Account toAccount) {
}

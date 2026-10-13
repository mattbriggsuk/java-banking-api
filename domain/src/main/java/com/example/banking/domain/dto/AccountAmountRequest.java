package com.example.banking.domain.dto;

import com.example.banking.domain.model.Transaction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** HTTP request wrapper shared by deposits and withdrawals. */
public class AccountAmountRequest extends RequestBase {

    @NotNull
    @Valid
    private Transaction transaction;

    public AccountAmountRequest() {
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}

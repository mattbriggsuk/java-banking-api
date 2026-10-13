package com.example.banking.domain.dto;

import com.example.banking.domain.model.Transaction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class AccountTransferRequest extends RequestBase {

    @NotNull
    @Valid
    private Transaction transaction;

    public AccountTransferRequest() {
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}

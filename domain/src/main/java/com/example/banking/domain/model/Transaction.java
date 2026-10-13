package com.example.banking.domain.model;

import com.example.banking.domain.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.Instant;

/** A requested money movement; sourceAccountNumber is required only for transfers. */
public class Transaction {

    @NotNull
    private TransactionType transactionType;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @Digits(integer = 16, fraction = 2)
    private BigDecimal amount;

    @Pattern(regexp = "[0-9]{16}", message = "must contain exactly 16 digits")
    private String sourceAccountNumber;

    @NotBlank
    @Pattern(regexp = "[0-9]{16}", message = "must contain exactly 16 digits")
    private String targetAccountNumber;

    private Instant transactionDate;

    // Required by Jackson when converting incoming JSON into a Transaction.
    public Transaction() {
    }

    public Transaction(TransactionType transactionType, BigDecimal amount,
                       String sourceAccountNumber, String targetAccountNumber,
                       Instant transactionDate) {
        this.transactionType = transactionType;
        this.amount = amount;
        this.sourceAccountNumber = sourceAccountNumber;
        this.targetAccountNumber = targetAccountNumber;
        this.transactionDate = transactionDate;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public String getTargetAccountNumber() {
        return targetAccountNumber;
    }

    public void setTargetAccountNumber(String targetAccountNumber) {
        this.targetAccountNumber = targetAccountNumber;
    }

    public Instant getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Instant transactionDate) {
        this.transactionDate = transactionDate;
    }
}

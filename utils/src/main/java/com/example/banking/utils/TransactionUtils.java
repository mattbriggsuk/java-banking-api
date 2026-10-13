package com.example.banking.utils;

import com.example.banking.domain.model.Account;
import com.example.banking.domain.model.Transaction;
import com.example.banking.domain.enums.TransactionType;

import java.math.BigDecimal;

/** Shared validation rules for banking transactions. */
public final class TransactionUtils {

    private TransactionUtils() {
        // Utility classes expose static methods and do not need instances.
    }

    /** Validates the common transaction fields and the rules for the expected type. */
    public static void validateTransaction(Transaction transaction, TransactionType expectedType) {

        if (transaction == null) {
            throw new IllegalArgumentException("Transaction is required");
        }

        if (transaction.getTransactionType() == null || transaction.getTransactionType() != expectedType) {
            throw new IllegalArgumentException("Transaction type must be " + expectedType);
        }

        BigDecimal amount = transaction.getAmount();
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2
                || amount.precision() - amount.scale() > 16) {
            throw new IllegalArgumentException(
                    "Amount must be positive with at most two decimal places and 16 integer digits");
        }

        if (!ValidationUtils.isValidAccountNumber(transaction.getTargetAccountNumber())) {
            throw new IllegalArgumentException("Target account number must be valid");
        }

        if (expectedType == TransactionType.TRANSFER) {
            if (!ValidationUtils.isValidAccountNumber(transaction.getSourceAccountNumber())) {
                throw new IllegalArgumentException("Source account number must be valid for transfers");
            }

            if (transaction.getSourceAccountNumber().equals(transaction.getTargetAccountNumber())) {
                throw new IllegalArgumentException("Source and target accounts must differ");
            }
        } else if (transaction.getSourceAccountNumber() != null
                && !transaction.getSourceAccountNumber().isBlank()) {
            throw new IllegalArgumentException("Source account number is only valid for transfers");
        }
    }

    /** Prevents a withdrawal or transfer from taking an account balance below zero. */
    public static void requireFunds(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
    }
}

package com.example.banking.domain.model;

import java.math.BigDecimal;

public class Account {
    private final String accountNumber;
    private final String customerNumber;
    private BigDecimal balance;
    private boolean closed;

    public Account(String accountNumber, String customerNumber,
                   BigDecimal balance, boolean closed) {
        this.accountNumber = accountNumber;
        this.customerNumber = customerNumber;
        this.balance = balance;
        this.closed = closed;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}

package com.example.banking.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AccountCloseRequest extends RequestBase {

    @NotBlank
    @Pattern(regexp = "[0-9]{16}", message = "must contain exactly 16 digits")
    private String accountNumber;

    public AccountCloseRequest() {
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}

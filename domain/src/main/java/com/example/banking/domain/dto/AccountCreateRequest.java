package com.example.banking.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AccountCreateRequest extends RequestBase {

    @NotBlank
    @Pattern(regexp = "[0-9]{16}", message = "must contain exactly 16 digits")
    private String customerNumber;

    public AccountCreateRequest() {
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }
}

package com.example.banking.domain.dto;

import com.example.banking.domain.model.Customer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class CustomerRegisterRequest extends RequestBase {

    @NotNull
    @Valid
    private Customer customer;

    public CustomerRegisterRequest() {
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}

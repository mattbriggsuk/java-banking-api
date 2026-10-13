package com.example.banking.domain.dto;

import com.example.banking.domain.model.Customer;

public record CustomerDto(String customerNumber, String firstName, String lastName, String email) {

    /** Creates an Protected API DTO without exposing the domain Customer object directly. */
    public static CustomerDto fromDomain(Customer customer) {
        return new CustomerDto(
                customer.customerNumber(),
                customer.firstName(),
                customer.lastName(),
                customer.email());
    }
}

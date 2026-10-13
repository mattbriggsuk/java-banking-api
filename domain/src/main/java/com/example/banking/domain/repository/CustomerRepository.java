package com.example.banking.domain.repository;

import com.example.banking.domain.model.Customer;

import java.util.Optional;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> getCustomer(String customerNumber);
}

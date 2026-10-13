package com.example.banking.repository;

import com.example.banking.domain.model.Customer;
import com.example.banking.domain.repository.CustomerRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Learning implementation: customers are lost when the application stops. */
// Registers this repository as a Spring bean so it can be injected into controllers. Note @Repository instead of @Service!
@Repository
public class CustomerRepositoryImpl implements CustomerRepository {

    private final Map<String, Customer> customers = new ConcurrentHashMap<>();

    @Override
    public Optional<Customer> getCustomer(String customerNumber) {
        return Optional.ofNullable(customers.get(customerNumber));
    }

    @Override
    public Customer save(Customer customer) {
        customers.put(customer.customerNumber(), customer);
        return customer;
    }
}

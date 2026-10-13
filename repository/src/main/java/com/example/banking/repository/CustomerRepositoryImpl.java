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

    // The customer number is the map key, allowing direct lookups without an internal ID.
    // ConcurrentHashMap safely supports access from multiple request threads.
    private final Map<String, Customer> customers = new ConcurrentHashMap<>();

    @Override
    public Optional<Customer> getCustomer(String customerNumber) {
        // Optional.empty() tells the service layer that no customer matched the supplied number.
        return Optional.ofNullable(customers.get(customerNumber));
    }

    @Override
    public Customer save(Customer customer) {
        // put() creates a new entry or replaces the customer with the same customer number.
        customers.put(customer.customerNumber(), customer);
        return customer;
    }
}

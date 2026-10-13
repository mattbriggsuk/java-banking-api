package com.example.banking.domain.service;

import com.example.banking.domain.model.Customer;
import com.example.banking.domain.model.Account;

import java.util.List;

public interface CustomerService {

    Customer register(Customer customer);
    Customer getCustomer(String customerNumber);
    List<Account> getCustomerAccounts(String customerNumber);

}

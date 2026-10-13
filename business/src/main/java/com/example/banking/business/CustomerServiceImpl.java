package com.example.banking.business;

import com.example.banking.domain.model.Customer;
import com.example.banking.domain.model.Account;
import com.example.banking.domain.repository.AccountRepository;
import com.example.banking.domain.repository.CustomerRepository;
import com.example.banking.domain.service.CustomerService;
import com.example.banking.utils.ValidationUtils;
import com.example.banking.utils.AccountUtils;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.List;

// Registers this business service as a Spring bean so it can be injected into controllers.
@Service
public class CustomerServiceImpl implements CustomerService
{

    //region Service Dependancies

        // Depend on the repository interface rather than a specific storage implementation.
        // "final" prevents reassignment of this reference (readonly in C#).
        private final CustomerRepository customerRepository;
        private final AccountRepository accountRepository;

    //endregion

    //region Constructor

        // Spring injects the repository implementation through this single constructor.
        // Note: No @Autowired annotation is needed when there is only one constructor.
        public CustomerServiceImpl(CustomerRepository customerRepository, AccountRepository accountRepository) {
            this.customerRepository = customerRepository;
            this.accountRepository = accountRepository;
        }

    //endregion

    // Get the Customer.
    @Override
    public Customer getCustomer(String customerNumber)
    {

        // Ensure customer number is valid.
        if (!ValidationUtils.isValidCustomerNumber(customerNumber)) {
            throw new IllegalArgumentException("Customer number must be a valid 16-digit number");
        }

        // Return the Customer object.
        return customerRepository.getCustomer(customerNumber)
                .orElseThrow(() -> new NoSuchElementException("Customer not found"));

    }

    // Get the Customer Accounts.
    @Override
    public List<Account> getCustomerAccounts(String customerNumber)
    {

        // Ensure customer number is valid.
        if (!ValidationUtils.isValidCustomerNumber(customerNumber)) {
            throw new IllegalArgumentException("Customer number must be a valid 16-digit number");
        }

        // Confirm the customer exists before returning their accounts.
        customerRepository.getCustomer(customerNumber)
                .orElseThrow(() -> new NoSuchElementException("Customer not found"));

        // Return the customer accounts.
        return accountRepository.getCustomerAccounts(customerNumber);

    }

    // Register a new Customer.
    @Override
    public synchronized Customer register(Customer customer) {

        // Ensure the customer object is valid.
        if (!ValidationUtils.isValidCustomerObject(customer)) {
            throw new IllegalArgumentException("Customer and all required details must be provided");
        }

        // Generate the new Customer object.
        Customer registeredCustomer = new Customer(
                AccountUtils.generateAccountNumber(),
                customer.firstName(),
                customer.lastName(),
                customer.email());

        // Save the customer object.
        return customerRepository.save(registeredCustomer);

    }

}

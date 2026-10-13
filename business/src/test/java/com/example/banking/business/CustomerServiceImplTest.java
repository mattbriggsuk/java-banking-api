package com.example.banking.business;

import com.example.banking.domain.model.Account;
import com.example.banking.domain.model.Customer;
import com.example.banking.domain.repository.AccountRepository;
import com.example.banking.domain.repository.CustomerRepository;
import com.example.banking.utils.ValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for customer business rules. Mock repositories isolate the service from storage
 * and let each test control whether customers and accounts exist.
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    // A fixed Luhn-valid customer number can pass the same validation used by production code.
    private static final String CUSTOMER_NUMBER = "1234567890123452";

    // Mockito supplies repository test doubles instead of the in-memory implementations.
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        // Direct construction avoids starting Spring and gives each test a fresh service instance.
        service = new CustomerServiceImpl(customerRepository, accountRepository);
    }

    @Test
    void getCustomerReturnsRepositoryCustomer() {
        Customer customer = registeredCustomer();
        when(customerRepository.getCustomer(CUSTOMER_NUMBER)).thenReturn(Optional.of(customer));

        Customer result = service.getCustomer(CUSTOMER_NUMBER);

        assertSame(customer, result);
    }

    @Test
    void getCustomerRejectsInvalidCustomerNumberBeforeRepositoryLookup() {
        assertThrows(IllegalArgumentException.class, () -> service.getCustomer("invalid"));

        // Validation should stop bad input before it reaches the persistence boundary.
        verify(customerRepository, never()).getCustomer("invalid");
    }

    @Test
    void getCustomerThrowsWhenCustomerDoesNotExist() {
        when(customerRepository.getCustomer(CUSTOMER_NUMBER)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getCustomer(CUSTOMER_NUMBER));
    }

    @Test
    void getCustomerAccountsReturnsAccountsForExistingCustomer() {
        List<Account> accounts = List.of(
                new Account("9876543210987658", CUSTOMER_NUMBER, new BigDecimal("25.00"), false));
        when(customerRepository.getCustomer(CUSTOMER_NUMBER))
                .thenReturn(Optional.of(registeredCustomer()));
        when(accountRepository.getCustomerAccounts(CUSTOMER_NUMBER)).thenReturn(accounts);

        List<Account> result = service.getCustomerAccounts(CUSTOMER_NUMBER);

        assertSame(accounts, result);
        verify(accountRepository).getCustomerAccounts(CUSTOMER_NUMBER);
    }

    @Test
    void getCustomerAccountsDoesNotQueryAccountsWhenCustomerDoesNotExist() {
        when(customerRepository.getCustomer(CUSTOMER_NUMBER)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.getCustomerAccounts(CUSTOMER_NUMBER));

        // Account lookup is unnecessary after the required customer lookup fails.
        verify(accountRepository, never()).getCustomerAccounts(CUSTOMER_NUMBER);
    }

    @Test
    void registerGeneratesCustomerNumberAndSavesCustomerDetails() {
        Customer request = new Customer(null, "Taylor", "Reed", "taylor.reed@example.com");
        // Imitate save semantics by returning the exact Customer passed to the mock.
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = service.register(request);

        // Capture the generated Customer so its number and copied details can be inspected.
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        Customer saved = captor.getValue();
        assertSame(saved, result);
        assertTrue(ValidationUtils.isValidCustomerNumber(saved.customerNumber()));
        assertEquals("Taylor", saved.firstName());
        assertEquals("Reed", saved.lastName());
        assertEquals("taylor.reed@example.com", saved.email());
    }

    @Test
    void registerRejectsMissingRequiredDetails() {
        Customer request = new Customer(null, " ", "Reed", "taylor.reed@example.com");

        assertThrows(IllegalArgumentException.class, () -> service.register(request));

        // Invalid registration data must never be saved.
        verify(customerRepository, never()).save(any());
    }

    private Customer registeredCustomer() {
        // Shared valid customer data keeps lookup tests concise.
        return new Customer(CUSTOMER_NUMBER, "Taylor", "Reed", "taylor.reed@example.com");
    }
}

package com.example.banking.business;

import com.example.banking.domain.enums.TransactionType;
import com.example.banking.domain.model.Account;
import com.example.banking.domain.model.Customer;
import com.example.banking.domain.model.Transaction;
import com.example.banking.domain.model.TransferResult;
import com.example.banking.domain.repository.AccountRepository;
import com.example.banking.domain.repository.CustomerRepository;
import com.example.banking.utils.ValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for account business rules. The repository interfaces are mocked, so these
 * tests exercise AccountServiceImpl without using Spring or the in-memory repositories.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    // Fixed Luhn-valid numbers keep validation realistic while making each test repeatable.
    private static final String CUSTOMER_NUMBER = "1234567890123452";
    private static final String SOURCE_ACCOUNT_NUMBER = "9876543210987658";
    private static final String TARGET_ACCOUNT_NUMBER = "1111111111111117";

    // Mockito creates these test doubles and records how the service interacts with them.
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    private AccountServiceImpl service;

    @BeforeEach
    void setUp() {
        // Construct the service directly to keep this a fast unit test with no Spring context.
        service = new AccountServiceImpl(accountRepository, customerRepository);
    }

    @Test
    void createGeneratesAndSavesAnOpenAccountForExistingCustomer() {
        // Arrange the repository responses required by the successful path.
        Customer customer = new Customer(CUSTOMER_NUMBER, "Taylor", "Reed", "taylor@example.com");
        when(customerRepository.getCustomer(CUSTOMER_NUMBER)).thenReturn(Optional.of(customer));
        // Return the same argument to imitate a repository that saves and returns an entity.
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act by calling the public business operation under test.
        Account result = service.create(CUSTOMER_NUMBER);

        // Assert both the returned state and the expected repository write.
        assertTrue(ValidationUtils.isValidAccountNumber(result.getAccountNumber()));
        assertEquals(CUSTOMER_NUMBER, result.getCustomerNumber());
        assertEquals(new BigDecimal("0.00"), result.getBalance());
        assertFalse(result.isClosed());
        verify(accountRepository).save(result);
    }

    @Test
    void createRejectsInvalidCustomerNumberBeforeRepositoryLookup() {
        assertThrows(IllegalArgumentException.class, () -> service.create("invalid"));

        // Invalid input must fail before any persistence work is attempted.
        verify(customerRepository, never()).getCustomer(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void createDoesNotSaveWhenCustomerDoesNotExist() {
        when(customerRepository.getCustomer(CUSTOMER_NUMBER)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.create(CUSTOMER_NUMBER));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void closeClosesAndSavesAccountWithZeroBalance() {
        Account account = account(SOURCE_ACCOUNT_NUMBER, "0.00", false);
        when(accountRepository.getAccount(SOURCE_ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account result = service.close(SOURCE_ACCOUNT_NUMBER);

        assertSame(account, result);
        assertTrue(account.isClosed());
        verify(accountRepository).save(account);
    }

    @Test
    void closeRejectsAccountWithNonZeroBalance() {
        Account account = account(SOURCE_ACCOUNT_NUMBER, "0.01", false);
        when(accountRepository.getAccount(SOURCE_ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

        assertThrows(IllegalStateException.class, () -> service.close(SOURCE_ACCOUNT_NUMBER));

        // A rejected close must leave the entity unchanged and must not persist it.
        assertFalse(account.isClosed());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void depositAddsAmountSetsTimestampAndSavesAccount() {
        Account account = account(TARGET_ACCOUNT_NUMBER, "10.00", false);
        Transaction transaction = transaction(TransactionType.DEPOSIT, "5.25", null, TARGET_ACCOUNT_NUMBER);
        when(accountRepository.getAccount(TARGET_ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account result = service.deposit(transaction);

        assertSame(account, result);
        assertEquals(new BigDecimal("15.25"), account.getBalance());
        assertNotNull(transaction.getTransactionDate());
        verify(accountRepository).save(account);
    }

    @Test
    void withdrawSubtractsAmountWhenFundsAreAvailable() {
        Account account = account(TARGET_ACCOUNT_NUMBER, "10.00", false);
        Transaction transaction = transaction(TransactionType.WITHDRAWAL, "4.00", null, TARGET_ACCOUNT_NUMBER);
        when(accountRepository.getAccount(TARGET_ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account result = service.withdraw(transaction);

        assertSame(account, result);
        assertEquals(new BigDecimal("6.00"), account.getBalance());
        assertNotNull(transaction.getTransactionDate());
        verify(accountRepository).save(account);
    }

    @Test
    void withdrawDoesNotSaveWhenFundsAreInsufficient() {
        Account account = account(TARGET_ACCOUNT_NUMBER, "3.00", false);
        Transaction transaction = transaction(TransactionType.WITHDRAWAL, "4.00", null, TARGET_ACCOUNT_NUMBER);
        when(accountRepository.getAccount(TARGET_ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

        assertThrows(IllegalStateException.class, () -> service.withdraw(transaction));

        // Verify failure is atomic from the service's point of view.
        assertEquals(new BigDecimal("3.00"), account.getBalance());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void depositRejectsClosedAccount() {
        Account account = account(TARGET_ACCOUNT_NUMBER, "10.00", true);
        Transaction transaction = transaction(TransactionType.DEPOSIT, "5.00", null, TARGET_ACCOUNT_NUMBER);
        when(accountRepository.getAccount(TARGET_ACCOUNT_NUMBER)).thenReturn(Optional.of(account));

        assertThrows(IllegalStateException.class, () -> service.deposit(transaction));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void transferMovesBalanceAndPersistsBothAccounts() {
        Account source = account(SOURCE_ACCOUNT_NUMBER, "20.00", false);
        Account target = account(TARGET_ACCOUNT_NUMBER, "7.50", false);
        Transaction transaction = transaction(
                TransactionType.TRANSFER, "4.50", SOURCE_ACCOUNT_NUMBER, TARGET_ACCOUNT_NUMBER);
        when(accountRepository.getAccount(SOURCE_ACCOUNT_NUMBER)).thenReturn(Optional.of(source));
        when(accountRepository.getAccount(TARGET_ACCOUNT_NUMBER)).thenReturn(Optional.of(target));

        TransferResult result = service.transfer(transaction);

        // A transfer updates both objects and persists them together through one repository call.
        assertSame(source, result.fromAccount());
        assertSame(target, result.toAccount());
        assertEquals(new BigDecimal("15.50"), source.getBalance());
        assertEquals(new BigDecimal("12.00"), target.getBalance());
        assertNotNull(transaction.getTransactionDate());
        verify(accountRepository).transferBalance(source, target);
    }

    @Test
    void transferRejectsSameSourceAndTargetBeforeRepositoryLookup() {
        Transaction transaction = transaction(
                TransactionType.TRANSFER, "4.50", SOURCE_ACCOUNT_NUMBER, SOURCE_ACCOUNT_NUMBER);

        assertThrows(IllegalArgumentException.class, () -> service.transfer(transaction));

        verify(accountRepository, never()).getAccount(any());
        verify(accountRepository, never()).transferBalance(any(), any());
    }

    @Test
    void transferDoesNotPersistWhenFundsAreInsufficient() {
        Account source = account(SOURCE_ACCOUNT_NUMBER, "2.00", false);
        Account target = account(TARGET_ACCOUNT_NUMBER, "7.50", false);
        Transaction transaction = transaction(
                TransactionType.TRANSFER, "4.50", SOURCE_ACCOUNT_NUMBER, TARGET_ACCOUNT_NUMBER);
        when(accountRepository.getAccount(SOURCE_ACCOUNT_NUMBER)).thenReturn(Optional.of(source));
        when(accountRepository.getAccount(TARGET_ACCOUNT_NUMBER)).thenReturn(Optional.of(target));

        assertThrows(IllegalStateException.class, () -> service.transfer(transaction));

        // Neither account may change when validation of the source balance fails.
        assertEquals(new BigDecimal("2.00"), source.getBalance());
        assertEquals(new BigDecimal("7.50"), target.getBalance());
        verify(accountRepository, never()).transferBalance(any(), any());
    }

    private Account account(String accountNumber, String balance, boolean closed) {
        // Central test-data builder keeps individual scenarios focused on their business rule.
        return new Account(accountNumber, CUSTOMER_NUMBER, new BigDecimal(balance), closed);
    }

    private Transaction transaction(TransactionType type, String amount,
                                    String sourceAccountNumber, String targetAccountNumber) {
        // The service supplies the server timestamp, so transactions begin with a null date.
        return new Transaction(type, new BigDecimal(amount), sourceAccountNumber,
                targetAccountNumber, null);
    }
}

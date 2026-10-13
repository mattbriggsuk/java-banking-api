package com.example.banking.api.controllers;

import com.example.banking.domain.dto.AccountAmountRequest;
import com.example.banking.domain.dto.AccountAmountResponse;
import com.example.banking.domain.dto.AccountCloseRequest;
import com.example.banking.domain.dto.AccountCloseResponse;
import com.example.banking.domain.dto.AccountCreateRequest;
import com.example.banking.domain.dto.AccountCreateResponse;
import com.example.banking.domain.dto.AccountTransferRequest;
import com.example.banking.domain.dto.AccountTransferResponse;
import com.example.banking.domain.service.AccountService;
import com.example.banking.domain.model.Account;
import com.example.banking.domain.model.Transaction;
import com.example.banking.domain.model.TransferResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

// Registers a Spring controller; returned DTOs are serialized to JSON.
@RestController

// Base route for these endpoints, similar to [Route] in ASP.NET Core.
@RequestMapping("/api/account")
public class AccountController {

    //region Services

        // Depend on the domain interface; the service handles the banking rules.
        private final AccountService accountService;

    //endregion

    //region Constructor

        // Spring supplies the implementation of the domain service interface.
        public AccountController(AccountService accountService) {
            this.accountService = accountService;
        }

    //endregion

    // Opens an account for an existing customer with a zero balance.
    // @PostMapping combines with the base route: POST /api/account/create.
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountCreateResponse create(@Valid @RequestBody AccountCreateRequest request) {

        try
        {

            // The service validates the customer and creates the domain account.
            Account account = accountService.create(request.getCustomerNumber());

            // The response factory hides the internal Account structure from the API.
            AccountCreateResponse response = AccountCreateResponse.fromDomain(account);
            return response;

        } catch (NoSuchElementException exception) {
            // The supplied customer does not exist.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        } catch (IllegalArgumentException exception) {
            // The supplied customer number is invalid.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }

    }

    // Closes an open account once its balance is zero.
    @PostMapping("/close")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AccountCloseResponse close(@Valid @RequestBody AccountCloseRequest request) {

        try
        {
            // Unwrap the request before passing the account number to the service.
            String accountNumber = request.getAccountNumber();
            Account account = accountService.close(accountNumber);

            // Map the updated domain account into the endpoint response.
            AccountCloseResponse response = AccountCloseResponse.fromDomain(account);
            return response;

        } catch (NoSuchElementException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }

    }

    // Adds a positive amount to an open account and returns its updated balance.
    @PostMapping("/deposit")
    public AccountAmountResponse deposit(@Valid @RequestBody AccountAmountRequest request) {

        try
        {

            // Unwrap the DTO; the service works with the Transaction model.
            Transaction transaction = request.getTransaction();
            Account account = accountService.deposit(transaction);

            // Return the updated account through the public DTO contract.
            AccountAmountResponse response = AccountAmountResponse.fromDomain(account);
            return response;

        } catch (NoSuchElementException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }

    }

    // Removes a positive amount from an open account if sufficient funds are available.
    @PostMapping("/withdraw")
    public AccountAmountResponse withdraw(@Valid @RequestBody AccountAmountRequest request) {

        try
        {

            // The service validates the transaction and available balance.
            Transaction transaction = request.getTransaction();
            Account account = accountService.withdraw(transaction);

            // Return the account state after the withdrawal.
            AccountAmountResponse response = AccountAmountResponse.fromDomain(account);
            return response;

        } catch (NoSuchElementException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }

    }

    // Moves funds between two open accounts in one operation.
    // Returns both updated accounts; the catch blocks map business errors to HTTP responses.
    @PostMapping("/transfer")
    public AccountTransferResponse transfer(@Valid @RequestBody AccountTransferRequest request) {

        try
        {

            // Unwrap the transfer instruction and let the service move the funds.
            Transaction transaction = request.getTransaction();
            TransferResult result = accountService.transfer(transaction);

            // Map both updated domain accounts into the transfer response.
            AccountTransferResponse response = AccountTransferResponse.fromDomain(result);
            return response;

        } catch (NoSuchElementException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }

    }

}

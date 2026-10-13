package com.example.banking.api.controllers;

import com.example.banking.domain.model.Account;
import com.example.banking.domain.service.CustomerService;
import com.example.banking.domain.dto.CustomerRegisterResponse;
import com.example.banking.domain.dto.CustomerRegisterRequest;
import com.example.banking.domain.dto.CustomerDto;
import com.example.banking.domain.dto.CustomerResponse;
import com.example.banking.domain.dto.CustomerAccountResponse;
import com.example.banking.domain.model.Customer;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import java.util.List;
import java.util.NoSuchElementException;

// Registers a Spring controller; returned DTOs are serialized to JSON.
@RestController

// Base route for all methods in this controller, similar to [Route] in ASP.NET Core.
@RequestMapping("/api/customer")
public class CustomerController {

    //region Services

        // Depend on the domain interface. "final" prevents reassignment, like C# readonly.
        private final CustomerService customerService;

    //endregion

    //region Constructor

        // Spring injects the CustomerService implementation through this constructor.
        // With a single constructor, no @Autowired annotation is needed.
        public CustomerController(CustomerService customerService) {
            this.customerService = customerService;
        }

    //endregion

    // Register a new Customer.
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerRegisterResponse register(@Valid @RequestBody CustomerRegisterRequest request) {

        try
        {

            // Unwrap the DTO so the business layer receives only the Customer model.
            Customer customer = customerService.register(request.getCustomer());

            // The response factory maps the domain customer and builds the wrapper.
            CustomerRegisterResponse response = CustomerRegisterResponse.fromDomain(customer);

            // Return the response.
            return response;

        } catch (NoSuchElementException exception) {
            // A valid customer number with no matching customer becomes HTTP 404.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        } catch (IllegalArgumentException exception) {
            // A malformed customer number becomes HTTP 400; unexpected failures still propagate.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }

    }

    // GET /api/customer/get?customerNumber=<16 digits>. @RequestParam is similar to C# [FromQuery].
    // A missing parameter or invalid customer number returns HTTP 400.
    @GetMapping("/get")
    public CustomerResponse get(@RequestParam("customerNumber") String customerNumber) {

        try
        {

            // The service returns the customer or throws when no customer exists.
            Customer customer = customerService.getCustomer(customerNumber);

            // The response factory maps the domain customer and builds the wrapper.
            CustomerResponse response = CustomerResponse.fromDomain(customer);

            // Return the response.
            return response;

        } catch (NoSuchElementException exception) {
            // A valid customer number with no matching customer becomes HTTP 404.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        } catch (IllegalArgumentException exception) {
            // A malformed customer number becomes HTTP 400; unexpected failures still propagate.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }

    }

    // GET /api/customer/accounts?customerNumber=<16 digits>.
    // An existing customer with no accounts receives HTTP 200 with an empty accounts list.
    @GetMapping("/accounts")
    public CustomerAccountResponse accounts(@RequestParam("customerNumber") String customerNumber) {

        try
        {

            // Get the customer accounts.
            List<Account> accounts = customerService.getCustomerAccounts(customerNumber);

            // The response factory maps domain accounts to DTOs and builds the wrapper.
            CustomerAccountResponse response = CustomerAccountResponse.fromDomain(
                    customerNumber,
                    accounts
                    );

            // Return the response.
            return response;

        }
        catch (NoSuchElementException exception)
        {
            // The customer number is valid, but no matching customer exists.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        } catch (IllegalArgumentException exception)
        {
            // The supplied customer number is invalid.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }

    }

}

package com.example.banking.domain.dto;

import com.example.banking.domain.model.Account;

import java.util.List;

public class CustomerAccountResponse extends ResponseBase {

    private String customerNumber;
    private List<AccountDto> accounts;

    public CustomerAccountResponse() {
    }

    /** Creates the response and maps domain accounts into API DTOs. */
    public static CustomerAccountResponse fromDomain(String customerNumber, List<Account> accounts) {
        CustomerAccountResponse response = new CustomerAccountResponse();
        response.setCustomerNumber(customerNumber);
        response.setAccounts(accounts.stream()
                .map(AccountDto::fromDomain)
                .toList());

        return response;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public List<AccountDto> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountDto> accounts) {
        this.accounts = accounts;
    }
}

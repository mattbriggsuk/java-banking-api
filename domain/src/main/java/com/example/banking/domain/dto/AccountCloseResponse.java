package com.example.banking.domain.dto;

import com.example.banking.domain.model.Account;

public class AccountCloseResponse extends ResponseBase {

    private AccountDto account;

    public AccountCloseResponse() {
    }

    public static AccountCloseResponse fromDomain(Account account) {
        AccountCloseResponse response = new AccountCloseResponse();
        response.setAccount(AccountDto.fromDomain(account));
        return response;
    }

    public AccountDto getAccount() {
        return account;
    }

    public void setAccount(AccountDto account) {
        this.account = account;
    }
}

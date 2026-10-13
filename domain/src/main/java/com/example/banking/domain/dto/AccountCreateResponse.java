package com.example.banking.domain.dto;

import com.example.banking.domain.model.Account;

public class AccountCreateResponse extends ResponseBase {

    private AccountDto account;

    public AccountCreateResponse() {
    }

    public static AccountCreateResponse fromDomain(Account account) {
        AccountCreateResponse response = new AccountCreateResponse();
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

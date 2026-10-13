package com.example.banking.domain.dto;

import com.example.banking.domain.model.Account;

public class AccountAmountResponse extends ResponseBase {

    private AccountDto account;

    public AccountAmountResponse() {
    }

    public static AccountAmountResponse fromDomain(Account account) {
        AccountAmountResponse response = new AccountAmountResponse();
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

package com.example.banking.domain.dto;

import com.example.banking.domain.model.TransferResult;

public class AccountTransferResponse extends ResponseBase {

    private AccountDto fromAccount;
    private AccountDto toAccount;

    public AccountTransferResponse() {
    }

    public static AccountTransferResponse fromDomain(TransferResult result) {
        AccountTransferResponse response = new AccountTransferResponse();
        response.setFromAccount(AccountDto.fromDomain(result.fromAccount()));
        response.setToAccount(AccountDto.fromDomain(result.toAccount()));
        return response;
    }

    public AccountDto getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(AccountDto fromAccount) {
        this.fromAccount = fromAccount;
    }

    public AccountDto getToAccount() {
        return toAccount;
    }

    public void setToAccount(AccountDto toAccount) {
        this.toAccount = toAccount;
    }
}

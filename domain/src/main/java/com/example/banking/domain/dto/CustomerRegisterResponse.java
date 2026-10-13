package com.example.banking.domain.dto;

import com.example.banking.domain.model.Customer;

public class CustomerRegisterResponse extends ResponseBase {

    private CustomerDto customer;

    public CustomerRegisterResponse() {
    }

    /** Creates the response and maps the domain customer into an API DTO. */
    public static CustomerRegisterResponse fromDomain(Customer customer) {
        CustomerRegisterResponse response = new CustomerRegisterResponse();
        response.setCustomer(CustomerDto.fromDomain(customer));
        return response;
    }

    public CustomerDto getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerDto customer) {
        this.customer = customer;
    }
}

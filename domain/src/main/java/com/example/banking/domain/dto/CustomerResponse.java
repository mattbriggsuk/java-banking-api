package com.example.banking.domain.dto;

import com.example.banking.domain.model.Customer;

public class CustomerResponse extends ResponseBase {

    private CustomerDto customer;

    public CustomerResponse() {
    }

    /** Creates the response and maps the domain customer into an API DTO. */
    public static CustomerResponse fromDomain(Customer customer) {
        CustomerResponse response = new CustomerResponse();
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

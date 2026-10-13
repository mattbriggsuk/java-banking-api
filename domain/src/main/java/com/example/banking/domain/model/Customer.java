package com.example.banking.domain.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
public record Customer(String customerNumber, @NotBlank String firstName, @NotBlank String lastName,
                       @NotBlank @Email String email) {
}

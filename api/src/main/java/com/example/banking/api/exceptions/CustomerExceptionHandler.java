package com.example.banking.api.exceptions;

import com.example.banking.api.controllers.CustomerController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Turn customer service validation errors into HTTP 400 responses.
@RestControllerAdvice(assignableTypes = CustomerController.class)
public class CustomerExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail badRequest(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
}

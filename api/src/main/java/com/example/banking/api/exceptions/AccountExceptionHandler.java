package com.example.banking.api.exceptions;

import com.example.banking.api.controllers.AccountController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.NoSuchElementException;

/** Translate account business errors to HTTP responses in the API layer. */
// Spring applies this advice only to requests handled by AccountController.
// Similar to a controller-scoped exception filter in ASP.NET Core.
@RestControllerAdvice(assignableTypes = AccountController.class)
public class AccountExceptionHandler {

    // Spring calls the matching handler when an exception escapes the controller.
    // A missing customer or account becomes HTTP 404 Not Found.
    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail notFound(NoSuchElementException exception) {
        // ProblemDetail supplies a JSON error body with the status and exception message.
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    // A conflicting account state, such as insufficient funds, becomes HTTP 409 Conflict.
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail conflict(IllegalStateException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    // Invalid business arguments, such as a missing transfer destination, become HTTP 400.
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail badRequest(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

}

package com.example.banking.utils;

import com.example.banking.domain.model.Customer;

/** Shared validation helpers with no dependency on Spring. */
public final class ValidationUtils {

    //region Constrictor

        private ValidationUtils() {
            // Utility classes expose static methods and do not need instances.
        }

    //endregion

    /**
     * Validates the 16-digit format and Luhn checksum shared by customer and account numbers.
     * The final digit is a check digit calculated from the preceding digits. Luhn validation
     * helps detect common typing mistakes; it does not prove that an account exists.
     */
    public static boolean isValidAccountNumber(String value) {
        if (value == null || !value.matches("[0-9]{16}")) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < value.length(); i++) {
            int digit = value.charAt(i) - '0';

            // For a 16-digit number, double digits in even zero-based positions.
            // Subtracting 9 reduces doubled values above 9 to their digit sum.
            if (i % 2 == 0) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
        }

        // A valid number has a total, including its check digit, divisible by 10.
        return sum % 10 == 0;
    }

    /** Customer numbers currently use the same 16-digit format as account numbers. */
    public static boolean isValidCustomerNumber(String value) {
        return isValidAccountNumber(value);
    }

    /** Ensures the customer and each required registration field contain a value. */
    public static boolean isValidCustomerObject(Customer customer) {
        return customer != null
                && isNotBlank(customer.firstName())
                && isNotBlank(customer.lastName())
                && isNotBlank(customer.email());
    }

    private static boolean isNotBlank(String value) {
        // isBlank() rejects empty strings and whitespace, like string.IsNullOrWhiteSpace in C#.
        return value != null && !value.isBlank();
    }

}

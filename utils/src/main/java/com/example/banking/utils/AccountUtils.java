package com.example.banking.utils;

import java.security.SecureRandom;

/** Shared helpers for customer and account number generation. */
public final class AccountUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    private AccountUtils() {
        // Utility classes expose static methods and do not need instances.
    }

    /** Generates 15 random digits followed by a Luhn check digit. */
    public static String generateAccountNumber() {
        StringBuilder digits = new StringBuilder(16);

        // Prevent the number from starting with zero so it is always visibly 16 digits.
        digits.append(RANDOM.nextInt(9) + 1);
        for (int i = 1; i < 15; i++) {
            digits.append(RANDOM.nextInt(10));
        }

        digits.append(calculateLuhnCheckDigit(digits));
        return digits.toString();
    }

    private static int calculateLuhnCheckDigit(CharSequence firstFifteenDigits) {
        int sum = 0;

        // For a 16-digit number, digits at even zero-based positions are doubled.
        for (int i = 0; i < firstFifteenDigits.length(); i++) {
            int digit = firstFifteenDigits.charAt(i) - '0';
            if (i % 2 == 0) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
        }

        return (10 - (sum % 10)) % 10;
    }
}

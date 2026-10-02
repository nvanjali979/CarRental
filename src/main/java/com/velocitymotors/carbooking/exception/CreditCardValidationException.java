package com.velocitymotors.carbooking.exception;

public class CreditCardValidationException extends RuntimeException {
    public CreditCardValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

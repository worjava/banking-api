package ru.astontest.bankingapi.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String m) {
        super(m);
    }
}

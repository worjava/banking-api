package ru.astontest.bankingapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EmptyNameException extends RuntimeException {
    public EmptyNameException(String message) {
        super(message);
    }
}

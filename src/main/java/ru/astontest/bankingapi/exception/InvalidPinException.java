package ru.astontest.bankingapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPinException extends RuntimeException {
    public InvalidPinException(String s) {
    super(s);}
}

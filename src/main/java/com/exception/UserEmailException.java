package com.exception;

public class UserEmailException extends EmailException {
    public UserEmailException(String mailSender, String message) {
        super(mailSender, message);
    }
}

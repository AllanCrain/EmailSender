package com.emailSender.exception;

public class FatalEmailException extends EmailException {

    public FatalEmailException(String mailSender, String message) {
        super(mailSender, message);
    }
}

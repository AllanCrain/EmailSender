package com.emailSender.exception;

public abstract class EmailException extends Exception {

    private final String mailSender;

    public EmailException(String mailSender, String message) {
        super(message);
        this.mailSender = mailSender;
    }

    public String getMailSender() {
        return mailSender;
    }
}

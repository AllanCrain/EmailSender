package com.emailSender.email;

import org.apache.commons.validator.routines.EmailValidator;

public class Email {

    public final String email;

    public Email(String email) {
        EmailValidator emailValidator = EmailValidator.getInstance();
        if (!emailValidator.isValid(email)) throw new IllegalArgumentException("email must be a valid");
        this.email = email;
    }

    @Override
    public String toString() {
        return "Email{" +
                "email='" + email + '\'' +
                '}';
    }
}

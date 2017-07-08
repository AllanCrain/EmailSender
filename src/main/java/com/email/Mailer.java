package com.email;

import com.Either;
import com.exception.FatalEmailException;

import java.util.Collection;
import java.util.concurrent.TimeoutException;

public abstract class Mailer {

    protected final ExecutionService executionService;
    protected final String           name;
    private         Mailer           nextMailer;

    public Mailer(String name) {
        this.name = name;
        this.executionService = new ExecutionService();
    }

    public void setNextMailer(Mailer nextMailer) {
        this.nextMailer = nextMailer;
    }

    /**
     * Sends email. It calls the `send` method to send the email and determine the next step processing should the
     * sending fails
     *
     * @param from    - from
     * @param to      - to list
     * @param cc      - cc list
     * @param bcc     - bcc list
     * @param subject - subject
     * @param body    - email body
     * @return - It will either return a message if successful or an exception
     */

    public Either<Exception, String> sendEmail(String from, final Collection<Email> to, final Collection<Email> cc, final Collection<Email> bcc, String subject, String body) {
        Either<Exception, String> either = send(from, to, cc, bcc, subject, body);
        if (either.hasLeft()) {
            if (either.getLeft() instanceof FatalEmailException || either.getLeft() instanceof TimeoutException) {
                if (nextMailer != null) return nextMailer.sendEmail(from, to, cc, bcc, subject, body);
            }
        }
        return either;
    }

    /**
     * Responsible for actually sending the email
     *
     * @param from    - from
     * @param to      - to list
     * @param cc      - cc list
     * @param bcc     - bcc list
     * @param subject - subject
     * @param body    - email body
     * @return - It will either return a message if successful or an exception
     */
    protected abstract Either<Exception, String> send(String from, final Collection<Email> to, final Collection<Email> cc, final Collection<Email> bcc, String subject, String body);

}

package com.email;


import com.Either;
import com.exception.FatalEmailException;
import com.exception.UserEmailException;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EmailTest {

    @Test
    public void testSuccessAndThenFailEmailSender() {
        Mailer successMailer = new SuccessEmail("success");
        successMailer.setNextMailer(new FatalErrorEmail("fatal1"));
        Either<Exception, String> either = successMailer.sendEmail("from", Collections.singletonList(new Email("test@test.com")), null, null, "test", "test");
        assertFalse(either.hasLeft());
        assertTrue(either.hasRight());
    }

    @Test
    public void testSuccessAndThenSuccessSender() {
        Mailer successMailer = new SuccessEmail("success1");
        successMailer.setNextMailer(new SuccessEmail("success2"));
        Either<Exception, String> either = successMailer.sendEmail("from", Collections.singletonList(new Email("test@test.com")), null, null, "test", "test");
        assertFalse(either.hasLeft());
        assertTrue(either.hasRight());
        assertTrue(either.getRight().equals("message send successfully from success1"));
    }


    @Test
    public void testFatalErrorAndThenSuccessEmailSender() {
        Mailer fatalErrorEmailer = new FatalErrorEmail("fata1");
        fatalErrorEmailer.setNextMailer(new SuccessEmail("success"));
        Either<Exception, String> either = fatalErrorEmailer.sendEmail("from", Collections.singletonList(new Email("test@test.com")), null, null, "test", "test");
        assertFalse(either.hasLeft());
        assertTrue(either.hasRight());
    }

    @Test
    public void testFatalErrorAndThenFataErrorMailSender() {
        Mailer fatalErrorEmailer = new FatalErrorEmail("fatal1");
        fatalErrorEmailer.setNextMailer(new FatalErrorEmail("fatal2"));
        Either<Exception, String> either = fatalErrorEmailer.sendEmail("from", Collections.singletonList(new Email("test@test.com")), null, null, "test", "test");
        assertTrue(either.hasLeft());
        assertFalse(either.hasRight());
        assertTrue(either.getLeft() instanceof FatalEmailException);
        FatalEmailException exception = (FatalEmailException) either.getLeft();
        assertTrue(exception.getMailSender().equals("fatal2"));
    }

    @Test
    public void testTimeOutAndThenSuccessMailSender() {
        Mailer timeOutEmailer = new TimeOutExceptionEmail("TimeOut");
        timeOutEmailer.setNextMailer(new SuccessEmail("success"));
        Either<Exception, String> either = timeOutEmailer.sendEmail("from", Collections.singletonList(new Email("test@test.com")), null, null, "test", "test");
        assertFalse(either.hasLeft());
        assertTrue(either.hasRight());
    }

    @Test
    public void testUserErrorAndThenSuccessMailSender() {
        Mailer userEmailError = new UserErrorEmail("UserError");
        userEmailError.setNextMailer(new SuccessEmail("success"));
        Either<Exception, String> either = userEmailError.sendEmail("from", Collections.singletonList(new Email("test@test.com")), null, null, "test", "test");
        assertTrue(either.hasLeft());
        assertFalse(either.hasRight());
    }

    private class SuccessEmail extends Mailer {

        SuccessEmail(String name) {
            super(name);
        }

        @Override
        protected Either<Exception, String> send(String from, Collection<com.email.Email> to, Collection<Email> cc, Collection<Email> bcc, String subject, String body) {
            return new Either<>(null, "message send successfully from " + name);
        }
    }

    private class FatalErrorEmail extends Mailer {

        FatalErrorEmail(String name) {
            super(name);
        }

        @Override
        protected Either<Exception, String> send(String from, Collection<Email> to, Collection<Email> cc, Collection<Email> bcc, String subject, String body) {
            return new Either<>(new FatalEmailException(name, "Fatal error in sending email"), null);
        }
    }

    private class UserErrorEmail extends Mailer {

        UserErrorEmail(String name) {
            super(name);
        }

        @Override
        protected Either<Exception, String> send(String from, Collection<Email> to, Collection<Email> cc, Collection<Email> bcc, String subject, String body) {
            return new Either<>(new UserEmailException("UserEmailErrorSender", "User Error in sending email"), null);
        }
    }

    private class TimeOutExceptionEmail extends Mailer {

        TimeOutExceptionEmail(String name) {
            super(name);
        }

        @Override
        protected Either<Exception, String> send(String from, Collection<Email> to, Collection<Email> cc, Collection<Email> bcc, String subject, String body) {
            return executionService.execute(() -> {
                Thread.sleep(2000);
                return null;
            }, 1, TimeUnit.SECONDS);
        }
    }

}

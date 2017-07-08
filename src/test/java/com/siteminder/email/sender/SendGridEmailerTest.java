package com.email.sender;

import com.Constants;
import com.Either;
import com.email.Email;
import com.email.ExecutionService;
import com.exception.UserEmailException;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

public class SendGridEmailerTest {

    private final SendGridEmailer sendGridEmailer;

    public SendGridEmailerTest() throws URISyntaxException {
        sendGridEmailer = new SendGridEmailer(System.getenv(Constants.SENDGRID_APIKEY));
    }

    @Test
    public void testSuccessEmailSending() throws MalformedURLException, URISyntaxException {
        Either<Exception, String> either = sendGridEmailer.send("test@hello.com", Collections.singletonList(new Email("tabiul@gmail.com")), null, null, "test email", "hello there from send grid");
        assertTrue(either.hasRight());
        assertTrue(either.getRight() != null);
    }


    @Test
    public void testMissingTo() throws MalformedURLException, URISyntaxException {
        Either<Exception, String> either = sendGridEmailer.send("test@hello.com", null, null, null, "test email", "hello there");
        assertTrue(either.hasLeft());
        assertTrue(either.getLeft() instanceof UserEmailException);
    }

}

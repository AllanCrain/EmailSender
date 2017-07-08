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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MailGunEmailerTest {

    private final MailGunEmailer mailGunEmailer;

    public MailGunEmailerTest() throws MalformedURLException, URISyntaxException {
        mailGunEmailer = new MailGunEmailer(System.getenv(Constants.MAILGUN_APIKEY),
                System.getenv(Constants.MAILGUN_DOMAIN));
    }

    @Test
    public void testSuccessEmailSending() throws MalformedURLException, URISyntaxException {
        Either<Exception, String> either = mailGunEmailer.send("test@hello.com", Collections.singletonList(new Email("tabiul@gmail.com")), null, null, "test email", "hello there from mailGun");
        assertTrue(either.hasRight());
        assertTrue(either.getRight() != null);
    }


    @Test
    public void testMissingTo() throws MalformedURLException, URISyntaxException {
        Either<Exception, String> either = mailGunEmailer.send("test@hello.com", null, null, null, "test email", "hello there");
        assertTrue(either.hasLeft());
        assertTrue(either.getLeft() instanceof UserEmailException);
        assertEquals(either.getLeft().getMessage(), "'to' parameter is missing");
    }
}

package com.email.sender;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.Either;
import com.email.Email;
import com.email.Mailer;
import com.exception.FatalEmailException;
import com.exception.UserEmailException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SendGridEmailer extends Mailer {

    private final CloseableHttpClient httpClient;
    private final HttpPost            httpPost;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger       logger       = Logger.getGlobal();
    private final String       url          = "https://api.sendgrid.com/v3/mail/send";

    public SendGridEmailer(String apiKey) {
        super("SendGrdi");
        if (apiKey == null) throw new IllegalArgumentException("apiKey is required");
        this.httpClient = HttpClients.createDefault();
        this.httpPost = new HttpPost(url);
        this.httpPost.setHeader("Authorization", "Bearer " + apiKey);
    }

    @Override
    protected Either<Exception, String> send(String from, Collection<Email> to, Collection<Email> cc, Collection<Email> bcc, String subject, String body) {
        logger.log(Level.INFO, " sending email through " + name);
        return executionService.execute(() -> {
            httpPost.setEntity(buildRequest(from, to, cc, bcc, subject, body));
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_ACCEPTED) {
                    logger.log(Level.FINE, EntityUtils.toString(response.getEntity()));
                    return new Either<>(null, "send successfully");
                } else if (statusLine.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                    logger.log(Level.SEVERE, EntityUtils.toString(response.getEntity()));
                    return new Either<>(new UserEmailException(name, "error sending email"), null);
                } else {
                    logger.log(Level.SEVERE, statusLine.getReasonPhrase());
                    return new Either<>(new FatalEmailException(name, statusLine.getReasonPhrase()), null);
                }
            }
            catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                return new Either<>(ex, null);
            }

        }, 1, TimeUnit.MINUTES);
    }

    private HttpEntity buildRequest(String from, Collection<Email> to, Collection<Email> cc, Collection<Email> bcc, String subject, String body) throws JsonProcessingException {
        Request request = new Request();
        Personalization personalization = new Personalization();
        request.personalizations.add(personalization);
        request.from = new SendGridEmail(from);
        if (to != null && !to.isEmpty()) {
            personalization.to.addAll(to.stream().map(email -> new SendGridEmail(email.email)).collect(Collectors.toList()));
        }
        if (cc != null && !cc.isEmpty()) {
            personalization.cc.addAll(cc.stream().map(email -> new SendGridEmail(email.email)).collect(Collectors.toList()));
        }
        if (bcc != null && !bcc.isEmpty()) {
            personalization.bcc.addAll(bcc.stream().map(email -> new SendGridEmail(email.email)).collect(Collectors.toList()));
        }
        request.subject = subject;
        request.contents.add(new Content("text/plain", body));
        return new StringEntity(objectMapper.writeValueAsString(request), ContentType.APPLICATION_JSON);
    }


    private static class Content {

        @JsonProperty("type")
        final String type;
        @JsonProperty("value")
        final String value;

        Content(String type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    private static class SendGridEmail {

        @JsonProperty("email")
        final String email;

        SendGridEmail(String email) {
            this.email = email;
        }
    }


    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private static class Personalization {

        @JsonProperty("to")
        List<SendGridEmail> to  = new ArrayList<>();
        @JsonProperty("cc")
        List<SendGridEmail> cc  = new ArrayList<>();
        @JsonProperty("bcc")
        List<SendGridEmail> bcc = new ArrayList<>();

    }

    private static class Request {

        @JsonProperty("personalizations")
        List<Personalization> personalizations = new ArrayList<>();
        @JsonProperty("from")
        SendGridEmail from;
        @JsonProperty("subject")
        String        subject;
        @JsonProperty("content")
        List<Content> contents = new ArrayList<>();

    }
}

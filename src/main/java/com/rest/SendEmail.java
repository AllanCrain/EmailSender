package com.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.Either;
import com.email.Email;
import com.email.Mailer;
import com.email.sender.MailGunEmailer;
import com.email.sender.SendGridEmailer;
import com.exception.UserEmailException;
import org.apache.http.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

public class SendEmail implements HttpRequestHandler {

    private final Mailer mailer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger       logger       = Logger.getGlobal();

    public SendEmail(String mailGunApiKey, String mailGunDomain, String sendGridApiKey) throws URISyntaxException, MalformedURLException {
        if (mailGunApiKey == null) throw new IllegalArgumentException("mailGunApiKey is required");
        if (mailGunDomain == null) throw new IllegalArgumentException("mailGunDomain is required");
        if (sendGridApiKey == null) throw new IllegalArgumentException("sendGridApiKey is required");

        mailer = new MailGunEmailer(mailGunApiKey, mailGunDomain);
        mailer.setNextMailer(new SendGridEmailer(sendGridApiKey));
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        String method = request.getRequestLine().getMethod();
        logger.log(Level.INFO, "serving request for SendEmail");
        if (method.equals("POST")) {
            if (request instanceof HttpEntityEnclosingRequest) {
                Response responseBody = new Response();
                try {
                    HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;
                    Request requestBody = objectMapper.readValue(EntityUtils.toString(entityRequest.getEntity()), Request.class);
                    logger.log(Level.INFO, "from: " + requestBody.from);
                    logger.log(Level.INFO, "to: " + requestBody.to);
                    logger.log(Level.INFO, "cc: " + requestBody.cc);
                    logger.log(Level.INFO, "bcc: " + requestBody.bcc);
                    logger.log(Level.INFO, "subject: " + requestBody.subject);
                    logger.log(Level.INFO, "body: " + requestBody.body);
                    Either<Exception, String> either = mailer.sendEmail(requestBody.from,
                            requestBody.to.stream().map(Email::new).collect(toList()),
                            requestBody.cc.stream().map(Email::new).collect(toList()),
                            requestBody.bcc.stream().map(Email::new).collect(toList()),
                            requestBody.subject,
                            requestBody.body
                    );
                    if (either.hasLeft()) {
                        if (either.getLeft() instanceof UserEmailException) {
                            response.setHeader("Content-Type", "application/json");
                            responseBody.message = either.getLeft().getMessage();
                            response.setStatusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);
                        } else {
                            logger.log(Level.SEVERE, either.getLeft().getMessage());
                            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                        }
                    } else {
                        response.setHeader("Content-Type", "application/json");
                        responseBody.message = "Successfully send email";
                        response.setEntity(new ByteArrayEntity(objectMapper.writeValueAsBytes(responseBody)));
                        response.setStatusCode(HttpStatus.SC_OK);
                    }
                }
                catch (IllegalArgumentException ex) {
                    response.setHeader("Content-Type", "application/json");
                    responseBody.message = "Invalid email";
                    response.setEntity(new ByteArrayEntity(objectMapper.writeValueAsBytes(responseBody)));
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                }
                catch (Exception ex) {
                    logger.log(Level.SEVERE, ex.getMessage());
                    response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            }
        } else {
            throw new MethodNotSupportedException(method + " method not supported");
        }
    }

    private static class Request {

        @JsonProperty("from")
        String from;
        @JsonProperty("to")
        List<String> to  = new ArrayList<>();
        @JsonProperty("bcc")
        List<String> bcc = new ArrayList<>();
        @JsonProperty("cc")
        List<String> cc  = new ArrayList<>();
        @JsonProperty("subject")
        String subject;
        @JsonProperty("body")
        String body;
    }

    private static class Response {

        @JsonProperty("message")
        String message;
    }

}

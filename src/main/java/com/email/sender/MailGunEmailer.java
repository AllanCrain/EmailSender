package com.email.sender;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.Either;
import com.email.Email;
import com.email.Mailer;
import com.exception.FatalEmailException;
import com.exception.UserEmailException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MailGunEmailer extends Mailer {

    private final Logger logger = Logger.getGlobal();
    private final CloseableHttpClient httpClient;
    private final HttpPost            httpPost;
    private final String       url          = "https://api.mailgun.net";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MailGunEmailer(String apiKey, String domain) throws URISyntaxException {
        super("MailGun");
        if (apiKey == null) throw new IllegalArgumentException("apiKey is required");
        if (domain == null) throw new IllegalArgumentException("domain is required");
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("api", apiKey);
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        this.httpClient = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build();
        this.httpPost = new HttpPost(String.format("%s/v3/%s/messages", url, domain));

    }

    @Override
    protected Either<Exception, String> send(String from, Collection<Email> to, Collection<Email> cc, Collection<Email> bcc, String subject, String body) {
        logger.log(Level.INFO, " sending email through " + name);
        return executionService.execute(() -> {
            List<NameValuePair> nameValuePairList = new ArrayList<>();
            nameValuePairList.add(new BasicNameValuePair("from", from));
            if (to != null && !to.isEmpty()) {
                nameValuePairList.add(new BasicNameValuePair("to", StringUtils.join(
                        to.stream().map(email -> email.email).collect(Collectors.toList()), ",")));
            }
            if (cc != null && !cc.isEmpty()) {
                nameValuePairList.add(new BasicNameValuePair("cc", StringUtils.join(
                        cc.stream().map(email -> email.email).collect(Collectors.toList()), ",")));
            }
            if (bcc != null && !bcc.isEmpty()) {
                nameValuePairList.add(new BasicNameValuePair("bcc", StringUtils.join(
                        bcc.stream().map(email -> email.email).collect(Collectors.toList()), ",")));
            }
            nameValuePairList.add(new BasicNameValuePair("subject", subject));
            nameValuePairList.add(new BasicNameValuePair("text", body));
            httpPost.setEntity(new UrlEncodedFormEntity(Collections.unmodifiableList(nameValuePairList)));
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    logger.log(Level.INFO, "success");
                    String responseEntity = EntityUtils.toString(response.getEntity());
                    Response res = objectMapper.readValue(responseEntity, Response.class);
                    return new Either<>(null, res.id);
                } else if (statusLine.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                    String responseEntity = EntityUtils.toString(response.getEntity());
                    logger.log(Level.SEVERE, responseEntity);
                    Response res = objectMapper.readValue(responseEntity, Response.class);
                    return new Either<>(new UserEmailException(name, res.message), null);
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



    private static class Response {

        @JsonProperty("id")
        String id;
        @JsonProperty("message")
        String message;
    }
}

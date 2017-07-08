package com;

import com.rest.FileHandler;
import com.rest.SendEmail;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final Logger logger = Logger.getGlobal();

    public static void main(String[] args) throws IOException, URISyntaxException {
        String mailGunAPIKey = System.getenv(Constants.MAILGUN_APIKEY);
        String sendGridAPIKey = System.getenv(Constants.SENDGRID_APIKEY);
        String mailGunDomain = System.getenv(Constants.MAILGUN_DOMAIN);
        if (mailGunAPIKey == null) {
            throw new IllegalStateException("Environment variable " + Constants.MAILGUN_APIKEY + " is required");
        }
        if (mailGunDomain == null) {
            throw new IllegalStateException("Environment variable " + Constants.MAILGUN_DOMAIN + " is required");
        }
        if (sendGridAPIKey == null) {
            throw new IllegalStateException("Environment variable " + Constants.SENDGRID_APIKEY + " is required");
        }
        HttpServer server = ServerBootstrap.bootstrap()
                .setListenerPort(8080)
                .registerHandler("/api/send", new SendEmail(mailGunAPIKey, mailGunDomain, sendGridAPIKey))
                .registerHandler("*", new FileHandler("/webapp"))
                .create();
        logger.log(Level.INFO, "listening on port 8080");
        server.start();
    }
}
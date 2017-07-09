# EmailSender

A simple app that allows to send email to multiple email provider

# Email Provider

Currently the following email providers are supported

* [MailGun](https://www.mailgun.com/)
* [SendGrid](https://sendgrid.com/)

# Setup

* clone the repo
* `./gradlew clean shadowJar` if linux
* `gradlew.bat clean shadowJar` if windows

# Technology

* Java 8
* Gradle
* Angular
* Bootstrap

In order to make the application simple, I did not used any framework. I used [Apache httpcomponents](https://hc.apache.org/)
both for the http client and the server.

# Running

* Setup account for **MailGun** and **SendGrid**
* Set the following environment variables
   * MAILGUN_APIKEY - MailGun private api key
   * MAILGUN_DOMAIN - MailGun Domain
   * SENDGRID_APIKEY - SendGrid api key
* Navigate to the folder **build/libs/** in and find the jar **mail-1.0-SNAPSHOT-all.jar**
* Run the jar via `java -jar mail-1.0-SNAPSHOT-all.jar`
* The application will start listening to port **8080**
* Navigate to the website via **http://localhost:8080**

# Feature

The application tries to send an email via multiple email providers. If a specific email provider fails then it tries the next
email provider.

# Design

The design pattern (Chain of Responsibility)[https://en.wikipedia.org/wiki/Chain-of-responsibility_pattern] is used.
Each Email provider must extend from abstract class `Mailer`. Two different type of exceptions is used to determine whether next email provider should be used if the previous email provider fails
* `UserEmailException` - This exception indicates some user error, as such there is no need to try the next email provider
* `FatalEmailException` - This exception indicates some fatal error happened when using the email provider as such the next email provider will be used in the chain.

# TODO

* Currently the port is hard coded both in ui and the code
* The error handling is very coarse. Better error handling is required in order to indicate to the user exactly what is the issue
* The ui does not have any proper validation
* The ui does not have the **cc**, **bcc** fields
* The message span in the ui does not close 
* Improve the user of Logger, currently using global logger
* Use Jetty or Spring Boot to get a more robust http client and server implementation :)
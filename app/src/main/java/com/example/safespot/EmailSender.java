package com.example.safespot;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {
    private static final String EMAIL = "itsafespot@gmail.com"; // Your email
    private static final String PASSWORD = "ocwt efpo duhw fqlr"; // Your app-specific password

    /**
     * Sends an email to a single recipient.
     *
     * @param recipient   The recipient's email address
     * @param subject     The subject of the email
     * @param messageBody The body of the email
     * @throws MessagingException If an error occurs during the sending process
     */
    public static void sendEmail(String recipient, String subject, String messageBody) throws MessagingException {
        // Configure SMTP server settings
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        // Create a session with an authenticator
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL, PASSWORD);
            }
        });

        // Create the email message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EMAIL));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        message.setSubject(subject);
        message.setText(messageBody);

        // Send the email
        Transport.send(message);
    }
}

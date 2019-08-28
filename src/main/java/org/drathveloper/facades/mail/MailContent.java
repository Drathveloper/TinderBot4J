package org.drathveloper.facades.mail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailContent {

    private String preloadSubject;

    private String preloadMessage;

    private String to;

    private String from;

    public MailContent(String preloadSubject, String preloadMessage, String to, String from) {
        this.preloadSubject = preloadSubject;
        this.preloadMessage = preloadMessage;
        this.to = to;
        this.from = from;
    }

    public Message getMessage(Session session, String... parameters) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(preloadSubject);
        message.setText(String.format(preloadMessage, parameters));
        return message;
    }
}

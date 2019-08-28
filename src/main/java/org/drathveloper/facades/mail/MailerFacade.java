package org.drathveloper.facades.mail;

import org.drathveloper.exceptions.MailerGenericException;
import org.drathveloper.facades.FileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class MailerFacade {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static MailerFacade instance;

    private MailContent mailGenerator;

    Session session;

    private MailerFacade() {
        String path = FileLoader.getInstance().getExecutionPath();
        try {
            FileInputStream fs = new FileInputStream(path + MailerConstants.CONFIG_FILE_NAME);
            Properties props = new Properties();
            props.load(fs);
            this.loadMessageContentFromProperties(props);
            this.generateSession(props);
            fs.close();
        } catch (IOException ex){
            logger.info("Couldn't find " + MailerConstants.CONFIG_FILE_NAME + ".");
            session = null;
        }
    }

    private void generateSession(Properties props){
        String user = props.getProperty(MailerConstants.USER_PROPERTY);
        String pass = props.getProperty(MailerConstants.PASS_PROPERTY);
        Authenticator auth = new SMTPAuthenticator(user, pass);
        session = session.getInstance(props, auth);
    }

    private void loadMessageContentFromProperties(Properties props) {
        String preloadSubject = props.getProperty(MailerConstants.PRELOAD_SUBJECT_PROPERTY);
        String preloadMessage = props.getProperty(MailerConstants.PRELOAD_MSG_PROPERTY);
        String to = props.getProperty(MailerConstants.PRELOAD_RECEIVERS_PROPERTY);
        String user = props.getProperty(MailerConstants.USER_PROPERTY);
        mailGenerator = new MailContent(preloadSubject, preloadMessage, to, user);
    }

    public static MailerFacade getInstance() {
        if(instance==null){
            instance = new MailerFacade();
        }
        return instance;
    }

    private boolean isMailerAvailable(){
        return session!=null;
    }

    public void sendMail(String... parameters) throws MailerGenericException {
        try {
            if(this.isMailerAvailable()) {
                Message message = mailGenerator.getMessage(session, parameters);
                Transport.send(message);
            } else {
                throw new MailerGenericException("Message couldn't be sent");
            }
        } catch(MessagingException ex){
            throw new MailerGenericException("Message couldn't be sent");
        }
    }
}

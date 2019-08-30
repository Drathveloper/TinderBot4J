package org.drathveloper.main;

import org.drathveloper.client.TinderClient;
import org.drathveloper.exceptions.HttpGenericException;
import org.drathveloper.exceptions.MailerGenericException;
import org.drathveloper.exceptions.NotEnoughLikesException;
import org.drathveloper.facades.db.MySQLFacade;
import org.drathveloper.facades.db.SQLFacade;
import org.drathveloper.facades.mail.MailerFacade;
import org.drathveloper.models.Match;
import org.drathveloper.models.MatchList;
import org.drathveloper.models.User;
import org.drathveloper.models.UserBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.List;

@SuppressWarnings("ALL")
class TinderBot4J implements Runnable {

    private TinderClient client;

    private SQLFacade dbClient;

    private MailerFacade mailClient;

    private final Logger logger = LoggerFactory.getLogger(TinderBot4J.class);

    private static final int NUM_AUTH_ATTEMPTS = 3;

    public TinderBot4J(){
        try {
            client = TinderClient.getInstance();
            dbClient = MySQLFacade.getInstance();
            mailClient = MailerFacade.getInstance();
        } catch(FileNotFoundException ex){
            logger.info("Required properties files not found:" +
                    "Mandatory files:\n" +
                    " - application.properties\n" +
                    " - fbtoken.properties (this one can be empty)");
        } catch(InvalidPropertiesFormatException ex){
            logger.info("application.properties entries dont have a valid format");
        }
    }

    @Override
    public void run() {
        logger.info("Starting Tinder bot");
        try {
            //this.executeAuthentication();
            this.executeBatchedLike();
        } catch (InterruptedException ex) {
            logger.info(ex.getMessage());
            System.exit(1);
        } catch (HttpGenericException | IOException ex) {
            logger.info("Authentication excedeed the maximum number of attempts: " + NUM_AUTH_ATTEMPTS);
            System.exit(1);
        }
    }

    private void executeAuthentication() throws HttpGenericException, IOException, InterruptedException {
        int attempts = 0;
        this.authenticate(attempts);
    }

    private void executeBatchedLike() throws IOException, HttpGenericException, InterruptedException {
        logger.info("Batched like processing started");
        UserBatch userBatch = null;
        boolean keepExecuting = true;
        while(keepExecuting) {
            try {
                int numLikes = client.getRemainingLikes();
                if(numLikes > 0) {
                    userBatch = client.getAvailableMatches();
                    printInfo(userBatch);
                    client.batchedLike(userBatch);
                    this.addToDatabase(userBatch);
                } else {
                    this.processNewMatches();
                    keepExecuting = false;
                }
            } catch (HttpGenericException ex){
                if(ex.getCause().getLocalizedMessage().equals("401")){
                    this.executeAuthentication();
                }
            } catch (NotEnoughLikesException ex){
                logger.info(ex.getMessage());
                keepExecuting = false;
            }
        }
        logger.info("Batched like processing ended");
    }

    private void processNewMatches() throws HttpGenericException {
        logger.info("Start handling new matches");
        MatchList matchesList = client.getMatchList();
        List<Match> unhandledMatches = matchesList.findMatchesWithoutConversation();
        for(Match match : unhandledMatches){
            client.sendMessage(match);
        }
        this.updateMatchesDatabase(matchesList);
        if(unhandledMatches.size() > 0){
            this.notifyNewMatches(unhandledMatches.size());
        }
        logger.info("End handling new matches");
    }

    private void notifyNewMatches(int numberMatches){
        logger.info("Start send notification via mail");
        try {
            mailClient.sendMail(String.valueOf(numberMatches));
        } catch(MailerGenericException ex){
            logger.info(ex.getMessage());
        }
        logger.info("End send notification via mail");
    }

    private void updateMatchesDatabase(MatchList matches){
        if(matches!=null){
            if(dbClient.isConnectionAvailable()){
                dbClient.updateMatches(matches);
            } else {
                logger.info("Database not available");
            }
        } else {
            logger.info("Nothing to add to database");
        }
    }

    private void addToDatabase(UserBatch userBatch){
        if(userBatch!=null){
            if(dbClient.isConnectionAvailable()) {
                dbClient.insertBatch(userBatch);
            } else {
                logger.info("Database not available");
            }
        } else {
            logger.info("Nothing to add to database");
        }
    }

    private void authenticate(int attempts) throws HttpGenericException, IOException, InterruptedException {
        logger.info("Authentication started. Attempts: " + attempts);
        try {
            client.loadTinderAuthToken();
            logger.info("Successfully authenticated");
        } catch (HttpGenericException ex){
            if(attempts < NUM_AUTH_ATTEMPTS){
                client.refreshFbToken();
                this.authenticate(++attempts);
            } else {
                logger.info("Reached maximum authentication attempts");
                throw ex;
            }
        }
    }

    private void printInfo(UserBatch userBatch){
        for(int i = 0; i< userBatch.size(); i++){
            User user = userBatch.getUser(i);
            logger.debug(
                    "--------------------------------------------------------------" +
                    "Tinder ID: " + user.getId() + "\n" +
                    "Name: " + user.getName() + "\n" +
                    "Bio: " + user.getBio() + "\n");
            logger.debug("Gallery list{\n");
            List<String> urls = user.getPhotoURL();
            for(String url : urls ){
                logger.debug("\t" + url + "\n");
            }
            logger.debug("}\n" +
                    "--------------------------------------------------------------");
        }
    }

}

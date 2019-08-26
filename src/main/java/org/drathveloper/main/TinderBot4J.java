package org.drathveloper.main;

import org.drathveloper.client.TinderClient;
import org.drathveloper.exceptions.HttpGenericException;
import org.drathveloper.exceptions.NotEnoughLikesException;
import org.drathveloper.facades.db.MySQLFacade;
import org.drathveloper.facades.db.SQLFacade;
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

    private final Logger logger = LoggerFactory.getLogger(TinderBot4J.class);

    private static final int NUM_AUTH_ATTEMPTS = 3;

    private static final long SLEEP_TIME = 3600000;

    public TinderBot4J(){
        try {
            client = TinderClient.getInstance();
            dbClient = MySQLFacade.getInstance();
        } catch(FileNotFoundException ex){
            logger.info("Required properties files not found\n" +
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
            this.executeAuthentication();
            while (true) {
                this.executeBatchedLike();
            }
        } catch (InterruptedException ex) {
            logger.info(ex.getMessage());
        } catch (HttpGenericException | IOException ex) {
            logger.info("Authentication excedeed the maximum number of attempts: " + NUM_AUTH_ATTEMPTS);
        }
    }

    private void executeAuthentication() throws HttpGenericException, IOException, InterruptedException {
        int attempts = 0;
        this.authenticate(attempts);
    }

    private void executeBatchedLike() throws InterruptedException, IOException, HttpGenericException {
        logger.info("Batched like processing started");
        UserBatch userBatch = null;
        try {
            int numLikes = client.getRemainingLikes();
            if (numLikes > 0) {
                userBatch = client.getAvailableMatches();
                printInfo(userBatch);
                client.batchedLike(userBatch);
            } else {
                logger.info("Sleeping: " + SLEEP_TIME + "ms");
                Thread.sleep(SLEEP_TIME);
            }
        } catch (HttpGenericException ex){
            if(ex.getCause().getLocalizedMessage().equals("401")){
                this.executeAuthentication();
            }
        } catch (NotEnoughLikesException ex){
            logger.info(ex.getMessage());
        }
        this.addToDatabase(userBatch);
        logger.info("Batched like processing ended");
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

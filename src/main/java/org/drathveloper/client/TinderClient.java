package org.drathveloper.client;

import com.google.gson.internal.LinkedTreeMap;
import org.drathveloper.exceptions.HttpGenericException;
import org.drathveloper.exceptions.NotEnoughLikesException;
import org.drathveloper.facades.JSONParserFacade;
import org.drathveloper.facades.HttpClientFacade;
import org.drathveloper.models.Match;
import org.drathveloper.models.MatchList;
import org.drathveloper.models.User;
import org.drathveloper.models.UserBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("unchecked")
public class TinderClient {

    private final Logger logger = LoggerFactory.getLogger(TinderClient.class);

    private static TinderClient instance = null;

    private String tinderToken;

    private ClientParameters parameters;

    private final JSONParserFacade jsonParser;

    private final HttpClientFacade client;

    private TinderClient() throws FileNotFoundException, InvalidPropertiesFormatException {
        client = HttpClientFacade.getInstance();
        jsonParser = JSONParserFacade.getInstance();
        parameters = new ClientParameters();
    }

    public static TinderClient getInstance() throws FileNotFoundException, InvalidPropertiesFormatException {
        if(instance == null){
            instance = new TinderClient();
        }
        return instance;
    }

    private Map<String, String> buildHeaders(boolean authenticated){
        Map<String, String> headers = new HashMap<>();
        if(tinderToken != null && authenticated){
            headers.put(ClientConstants.AUTH_HEADER, tinderToken);
        }
        headers.put(ClientConstants.CONTENT_TYPE_HEADER, ClientConstants.DEFAULT_CONTENT_TYPE);
        headers.put(ClientConstants.USER_AGENT_HEADER, parameters.getUserAgent());
        return headers;
    }

    public void refreshFbToken() throws IOException, InterruptedException {
        logger.info("Start FB token refresh");
        logger.debug("FB Token: " + parameters.getFbToken());
        parameters.refreshFbToken();
        logger.debug("Refreshed FB Token: " + parameters.getFbToken());
        logger.info("End FB token refresh");
    }

    public void loadTinderAuthToken() throws HttpGenericException {
        logger.info("Start Tinder authentication process");
        Map<String, String> headers = this.buildHeaders(false);
        Map<String, String> body = new HashMap<>();
        body.put("token", parameters.getFbToken());
        String jsonBody = jsonParser.objectToJSON(body);
        String url = parameters.getURL(ClientConstants.AUTH_MAPPING);
        Map<String, Object> response = jsonParser.jsonToMap(client.executePost(url, headers, jsonBody));
        tinderToken = (String) ((Map<String, Object>) response.get("data")).get("api_token");
        logger.debug("FB Token: " + parameters.getFbToken());
        logger.debug("Tinder Token: " + tinderToken);
        logger.info("End Tinder authentication process.");
    }

    public int getRemainingLikes() throws HttpGenericException{
        logger.info("Start checking remaining likes");
        Map<String, String> headers = this.buildHeaders(true);
        String url = parameters.getURL(ClientConstants.METADATA_MAPPING);
        Map<String, Object> output = jsonParser.jsonToMap(client.executeGet(url, headers));
        int remainingLikes = ((Double)((LinkedTreeMap<String, Object>)output.get("rating")).get("likes_remaining")).intValue();
        logger.info("Likes remaining: " + remainingLikes + "\nEnd checking remaining likes");
        return remainingLikes;
    }

    public void batchedLike(UserBatch availableMatches) throws InterruptedException, HttpGenericException, NotEnoughLikesException {
        logger.info("Start batch user liking/passing");
        RngTools rng = new RngTools();
        List<Boolean> flagList = rng.generateWeightedRandomDistribution(availableMatches.size(), parameters.getLikePercent());
        int index = 0;
        for(boolean flag : flagList){
            User likableUser = availableMatches.getUser(index);
            if(flag){
                try {
                    this.likeProfile(likableUser);
                } catch(NotEnoughLikesException ex){
                    availableMatches.removeRange(index + 1, availableMatches.size());
                    try {
                        logger.info("Not enough likes, trying super like");
                        this.superLikeProfile(likableUser);
                        break;
                    } catch(NotEnoughLikesException e){
                        logger.info("Not enough likes & super likes, exiting");
                        availableMatches.removeIndex(index);
                        throw new NotEnoughLikesException("Not enough likes & superlikes");
                    }
                }
            } else {
                this.dislikeProfile(likableUser);
            }
            int sleepTime = rng.generateRandomDelay(1000, 10000);
            Thread.sleep(sleepTime);
            index++;
        }
        logger.info("End batch user liking/passing");
    }

    public void dislikeProfile(User user) throws HttpGenericException {
        logger.info("Start passing user " + user.getId());
        Map<String, String> headers = this.buildHeaders(true);
        String url = String.format(parameters.getURL(ClientConstants.DISLIKE_MAPPING), user.getId());
        client.executeGet(url, headers);
        user.setLiked(false);
        logger.info("End passing user " + user.getId());
    }

    public void likeProfile(User user) throws HttpGenericException, NotEnoughLikesException {
        logger.info("Start liking user " + user.getId());
        Map<String, String> headers = this.buildHeaders(true);
        String url = String.format(parameters.getURL(ClientConstants.LIKE_MAPPING), user.getId());
        Map<String, Object> output = jsonParser.jsonToMap(client.executeGet(url, headers));
        user.setLiked(true);
        if(((Double) output.get("likes_remaining")) <= 0){
            throw new NotEnoughLikesException("You dont have enough likes");
        }
        logger.info("End liking user " + user.getId());
        user.setMatch(output.get("match").equals(true));
    }

    public void superLikeProfile(User user) throws HttpGenericException, NotEnoughLikesException {
        logger.info("Start super liking user " + user.getId());
        Map<String, String> headers = this.buildHeaders(true);
        String url = String.format(parameters.getURL(ClientConstants.SUPER_LIKE_MAPPING), user.getId());
        Map<String, Object> output = jsonParser.jsonToMap(client.executePost(url, headers, null));
        if(output.get("limit_exceeded")==null){
            logger.info("End super liking user " + user.getId());
            user.setLiked(true);
        } else {
            logger.info("No super like available");
            throw new NotEnoughLikesException("No super like available");
        }
    }

    public UserBatch getAvailableMatches() throws HttpGenericException {
        logger.info("Start getting available matches");
        UserBatch availableUsers;
        Map<String, String> headers = this.buildHeaders(true);
        String url = parameters.getURL(ClientConstants.CANDIDATES_MAPPING);
        Map<String, Object> output = jsonParser.jsonToMap(client.executeGet(url, headers));
        List<Object> results = (List<Object>) output.get("results");
        availableUsers = new UserBatch(results);
        logger.info("Found possible matches: " + availableUsers.size() + "\nEnd getting available matches");
        return availableUsers;
    }

    public void sendMessage(Match match) throws HttpGenericException {
        logger.info("Start send message to " + match.getUserId());
        Map<String, String> headers = this.buildHeaders(true);
        String url = String.format(parameters.getURL(ClientConstants.SEND_MSG_MAPPING), match.getMatchId());
        Map<String, String> body = new HashMap<>();
        String message = parameters.getMessageFromPreloaded();
        body.put("message", message);
        Map<String, Object> output = jsonParser.jsonToMap(client.executePost(url, headers, jsonParser.objectToJSON(body)));
        logger.info("Message sent to " + output.get("_id") + "\nEnd send message.");
    }

    public String getMatchIdByUserId(String id) throws HttpGenericException {
        logger.info("Start get match id from user id");
        Map<String, String> headers = this.buildHeaders(true);
        String url = parameters.getURL(ClientConstants.MATCHES_MAPPING);
        String jsonBody = "{\"last_activity_date\":\"\",\"nudge\":false}";
        Map<String, Object> output = jsonParser.jsonToMap(client.executePost(url, headers, jsonBody));
        List<LinkedTreeMap> matches = ((ArrayList<LinkedTreeMap>)output.get("matches"));
        for (LinkedTreeMap match : matches) {
            String userId = ((List<String>) match.get("participants")).get(0);
            if (userId.equals(id)) {
                String matchId = (String) match.get("_id");
                logger.debug("Found match id from user id: " + matchId);
                logger.info("End get match id from user id");
                return matchId;
            }
        }
        logger.info("Match id not found");
        logger.info("End get match id from user id");
        return null;
    }

    public MatchList getMatchList() throws HttpGenericException {
        logger.info("Start get matches list");
        Map<String, String> headers = this.buildHeaders(true);
        String url = parameters.getURL(ClientConstants.MATCHES_MAPPING);
        String jsonBody = "{\"last_activity_date\":\"\",\"nudge\":false}";
        Map<String, Object> output = jsonParser.jsonToMap(client.executePost(url, headers, jsonBody));
        List<Object> matches = (ArrayList<Object>) output.get("matches");
        MatchList list = new MatchList(matches);
        logger.info("End get matches list");
        return list;
    }

}

package org.drathveloper.client;

import org.drathveloper.facades.FileLoader;
import org.drathveloper.models.FacebookLogin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

class ClientParameters {

    private final FileLoader fileLoader = FileLoader.getInstance();

    private String fbToken;

    private String baseURL;

    private double likePercent;

    private String userAgent;

    private FacebookLogin fbLoginData;

    private Map<String, String> endpoints;

    private List<String> preloadMessages;

    public ClientParameters() throws FileNotFoundException, InvalidPropertiesFormatException {
        String path = fileLoader.getExecutionPath();
        try {
            this.loadProperties(path);
        } catch(NumberFormatException ex){
            throw new NumberFormatException(ex.getMessage());
        }
        this.loadFbToken(path);
    }

    private void loadProperties(String path) throws FileNotFoundException, InvalidPropertiesFormatException {
        Map<String, String> props = fileLoader.readPropertiesFile(path + ClientConstants.APPLICATION_CONFIG_FILE);
        if(this.isValidPropertiesMap(props)) {
            this.setProperties(props);
            this.loadFbLoginData(props);
            this.setEndpoints(props);
            this.setPreloadMessages(props);
        } else {
            throw new InvalidPropertiesFormatException("Properties file dont have mandatory fields");
        }
    }

    private void loadFbLoginData(Map<String, String> props) throws InvalidPropertiesFormatException {
        String user = props.get(ClientConstants.FB_LOGIN_USER);
        String pass = props.get(ClientConstants.FB_LOGIN_PASS);
        if(user!=null && pass!=null) {
            fbLoginData = new FacebookLogin(user, pass);
        } else {
            throw new InvalidPropertiesFormatException("Properties file dont have facebook login info");
        }
    }

    private void loadFbToken(String path) throws FileNotFoundException {
        Map<String, String> fbProperties = fileLoader.readPropertiesFile(path + ClientConstants.FB_TOKEN_FILE);
        this.setFbToken(fbProperties);
    }

    public void refreshFbToken() throws IOException, InterruptedException {
        String path = FileLoader.getInstance().getExecutionPath();
        String command = "node " + path + "main.js --path " + path + " --user " + fbLoginData.getUser() + " --pass " + fbLoginData.getPass();
        Process p = Runtime.getRuntime().exec(command);
        if(p.waitFor() == 0) {
            this.loadFbToken(path);
        } else {
            throw new RuntimeException("Unrecoverable error");
        }
    }

    private void setFbToken(Map<String, String> fbProperties){
        this.fbToken = fbProperties.get(ClientConstants.FB_TOKEN);
    }

    private void setProperties(Map<String, String> props) throws InvalidPropertiesFormatException {
        try {
            this.baseURL = props.get(ClientConstants.URL_PARAMETER);
            this.likePercent = Double.parseDouble(props.get(ClientConstants.LIKE_PERCENTAGE));
            this.userAgent = props.get(ClientConstants.USER_AGENT);
        } catch(NullPointerException | NumberFormatException ex){
            throw new InvalidPropertiesFormatException("Properties file have fields with wrong format");
        }
    }

    private void setEndpoints(Map<String, String> props){
        endpoints = new HashMap<>();
        endpoints.put(ClientConstants.AUTH_MAPPING, props.get(ClientConstants.AUTH_ENDPOINT));
        endpoints.put(ClientConstants.LIKE_MAPPING, props.get(ClientConstants.LIKE_ENDPOINT));
        endpoints.put(ClientConstants.DISLIKE_MAPPING, props.get(ClientConstants.DISLIKE_ENDPOINT));
        endpoints.put(ClientConstants.CANDIDATES_MAPPING, props.get(ClientConstants.CANDIDATES_ENDPOINT));
        endpoints.put(ClientConstants.METADATA_MAPPING, props.get(ClientConstants.METADATA_ENDPOINT));
        endpoints.put(ClientConstants.SUPER_LIKE_MAPPING, props.get(ClientConstants.SUPER_LIKE_ENDPOINT));
        endpoints.put(ClientConstants.SEND_MSG_MAPPING, props.get(ClientConstants.SEND_MSG_ENDPOINT));
        endpoints.put(ClientConstants.PROFILE_MAPPING, props.get(ClientConstants.PROFILE_ENDPOINT));
        endpoints.put(ClientConstants.MATCHES_MAPPING, props.get(ClientConstants.MATCHES_ENDPOINT));
    }

    private void setPreloadMessages(Map<String, String> props){
        preloadMessages = new ArrayList<>();
        for(String key : props.keySet()){
            if(key.contains(ClientConstants.PRELOAD_MESSAGE)){
                preloadMessages.add(props.get(key));
            }
        }
    }

    private boolean isValidPropertiesMap(Map<String, String> props) {
        for(Map.Entry<String, String> entry : props.entrySet()){
            if(entry.getValue()==null){
                return false;
            }
        }
        return true;
    }

    public String getURL(String endpoint){
        return baseURL + endpoints.get(endpoint);
    }

    public String getFbToken() {
        return fbToken;
    }

    public double getLikePercent() {
        return likePercent;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getMessageFromPreloaded(){
        if(preloadMessages.size() > 0){
            Collections.shuffle(preloadMessages);
            return preloadMessages.get(0);
        }
        return ClientConstants.DEFAULT_MESSAGE;
    }

}

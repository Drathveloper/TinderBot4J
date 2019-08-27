package org.drathveloper.models;

import java.util.List;

public class Match {

    private String matchId;

    private String userId;

    private List<Message> messages;

    public Match(){

    }

    public Match(String matchId, List<Message> messages, String userId){
        this.matchId = matchId;
        this.messages = messages;
        this.userId = userId;
    }

    public boolean isConversationStarted(){
        for(Message message : messages){
            if(!message.isSentByTheMatch(userId)){
                return true;
            }
        }
        return false;
    }


    public int getMessageCount(){
        return messages.size();
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public List<Message>  getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

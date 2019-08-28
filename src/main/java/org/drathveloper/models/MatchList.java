package org.drathveloper.models;

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;

public class MatchList {

    private List<Match> matches;

    public MatchList(List<Object> rawMatchesList){
        this.mapToObject(rawMatchesList);
    }

    public int size(){
        return matches.size();
    }

    public Match getMatch(int index){
        return matches.get(index);
    }

    public void removeMatch(int index){
        matches.remove(index);
    }

    public void addMatch(Match match){
        matches.add(match);
    }

    public List<Match> findMatchesWithoutConversation(){
        List<Match> unhandledMatches = new ArrayList<>();
        for(Match match : matches){
            if(match.getMessageCount() == 0 || !match.isConversationStarted()){
                unhandledMatches.add(match);
            }
        }
        return unhandledMatches;
    }

    private void mapToObject(List<Object> objectList){
        List<Match> matches = new ArrayList<>();
        for(Object obj : objectList){
            matches.add(this.mapMatchFromObject(obj));
        }
        this.matches = matches;
    }

    private Match mapMatchFromObject(Object object){
        String matchId = (String) ((LinkedTreeMap) object).get("_id");
        List<Message> messagesList = new ArrayList<>();
        List<LinkedTreeMap<String, Object>> messages = (List<LinkedTreeMap<String, Object>>)((LinkedTreeMap) object).get("messages");
        for(int i=0; i<messages.size(); i++){
            String from = (String) messages.get(i).get("from");
            String to = (String) messages.get(i).get("to");
            String message = (String) messages.get(i).get("message");
            messagesList.add(new Message(from, to, message));
        }
        String userId = ((List<String>)((LinkedTreeMap) object).get("participants")).get(0);
        return new Match(matchId, messagesList, userId);
    }
}

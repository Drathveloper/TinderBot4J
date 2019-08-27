package org.drathveloper.models;

public class Message {

    private String from;

    private String to;

    private String message;

    public Message(String from, String to, String message){
        this.from = from;
        this.to = to;
        this.message = message;
    }

    public Message(){

    }

    public boolean isSentByTheMatch(String personId){
        return from.equals(personId);
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

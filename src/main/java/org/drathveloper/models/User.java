package org.drathveloper.models;

import java.util.List;

public class User {

    private String id;

    private String name;

    private String bio;

    private List<String> photoURL;

    private boolean isTraveling;

    private boolean isMatch;

    private boolean isLiked;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<String> getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(List<String> photoURL) {
        this.photoURL = photoURL;
    }

    public boolean isTraveling() {
        return isTraveling;
    }

    public void setTraveling(boolean traveling) {
        isTraveling = traveling;
    }

    public boolean isMatch() {
        return isMatch;
    }

    public void setMatch(boolean match) {
        isMatch = match;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }
}

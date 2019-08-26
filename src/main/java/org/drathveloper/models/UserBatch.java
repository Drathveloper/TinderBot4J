package org.drathveloper.models;

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;

public class UserBatch {

    private final List<User> users;

    private UserBatch(){
        this.users = new ArrayList<>();
    }

    public UserBatch(List<Object> objects){
        this();
        this.mapToObject(objects);
    }

    public User getUser(int index){
        return users.get(index);
    }

    public void addUser(User user){
        users.add(user);
    }

    public int size(){
        return users.size();
    }

    private void mapToObject(List<Object> elements){
        for(Object element : elements){
            users.add(this.mapMatchToUser(element));
        }
    }

    public void removeRange(int start, int end){
        users.subList(start, end).clear();
    }

    @SuppressWarnings("unchecked")
    private User mapMatchToUser(Object object){
        User user = new User();
        if(object!=null) {
            LinkedTreeMap<String, Object> userMap = (LinkedTreeMap<String, Object>) object;
            user.setId((String) userMap.get("_id"));
            user.setBio((String) userMap.get("bio"));
            user.setName((String) userMap.get("name"));
            user.setTraveling(userMap.get("is_traveling") != null && (boolean) userMap.get("is_traveling"));
            List<String> photoURL = new ArrayList<>();
            List<LinkedTreeMap<String, Object>> photoList = (List<LinkedTreeMap<String, Object>>) userMap.get("photos");
            for (LinkedTreeMap<String, Object> stringObjectLinkedTreeMap : photoList) {
                photoURL.add((String) stringObjectLinkedTreeMap.get("url"));
            }
            user.setPhotoURL(photoURL);
        }
        return user;
    }
}

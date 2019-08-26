package org.drathveloper.facades;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

public class JSONParserFacade {

    private static JSONParserFacade instance;

    private final Gson jsonParser;

    private JSONParserFacade(){
        GsonBuilder builder = new GsonBuilder();
        jsonParser = builder.create();
    }

    public static JSONParserFacade getInstance(){
        if(instance == null){
            instance = new JSONParserFacade();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> jsonToMap(String json){
        return jsonParser.fromJson(json, Map.class);
    }

    public String objectToJSON(Object object){
        return jsonParser.toJson(object);
    }
}

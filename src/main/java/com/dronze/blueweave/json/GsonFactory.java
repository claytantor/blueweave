package com.dronze.blueweave.json;


import com.dronze.tradestation.json.deserialize.GuavaTableDeserializer;
import com.dronze.tradestation.json.deserialize.ModelTickerDeserializer;
import com.dronze.tradestation.json.serialize.GuavaTableSerializer;
import com.dronze.tradestation.json.serialize.TickerModelSerializer;
import com.dronze.tradestation.model.Ticker;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.TreeBasedTable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * Created by claytongraham on 12/7/15.
 */
public class GsonFactory {

    public enum Type{
        DEFAULT,
        MODEL,
        USER,
        WIKI,
    }

    protected static Gson makeSerializerGson(Type type, Boolean pretty){
        GsonBuilder gsonBuilder = new GsonBuilder();
        if(pretty)
            gsonBuilder.setPrettyPrinting();

        switch (type){
            case MODEL:
                gsonBuilder.setDateFormat("yyyy-MM-dd");
                gsonBuilder.registerTypeAdapter(Ticker.class, new TickerModelSerializer());
                break;
            case DEFAULT:
            default:
                gsonBuilder.registerTypeAdapter(HashBasedTable.class, new GuavaTableSerializer());
                gsonBuilder.registerTypeAdapter(TreeBasedTable.class, new GuavaTableSerializer());
                break;
        }
        return gsonBuilder.create();
    }

    protected static Gson makeDeserializerGson(Type type){
        GsonBuilder gsonBuilder = new GsonBuilder();
        switch (type){
            case MODEL:
                gsonBuilder.registerTypeAdapter(Ticker.class, new ModelTickerDeserializer());
                break;
            case DEFAULT:
            default:
                gsonBuilder.setDateFormat("yyyy-MM-dd");
                gsonBuilder.registerTypeAdapter(HashBasedTable.class, new GuavaTableDeserializer());
                gsonBuilder.registerTypeAdapter(TreeBasedTable.class, new GuavaTableDeserializer());
                break;



        }
        return gsonBuilder.create();
    }

    public static String toJson(Object entity, Type type){
        return makeSerializerGson(type, false).toJson(entity);
    }

    public static JsonElement toJsonElement(Object entity, Type type){
        return makeSerializerGson(type, false).toJsonTree(entity);
    }

    public static String toJson(Object entity, Type type, Boolean pretty){
        return makeSerializerGson(type, pretty).toJson(entity);
    }

    public static <T> T fromJson(String json, Class<T> classOfT, Type type){
        return makeDeserializerGson(type).fromJson(json, classOfT);
    }



}
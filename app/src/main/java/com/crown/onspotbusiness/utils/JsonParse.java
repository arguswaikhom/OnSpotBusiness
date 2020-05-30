package com.crown.onspotbusiness.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonParse {
    public static JSONObject stringToObject(String string) {
        try {
            return new JSONObject(string);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public static String stringFromObject(JSONObject jsonObject, String paramName) {
        try {
            return jsonObject.getString(paramName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Double numberFromObject(JSONObject jsonObject, String paramName) {
        try {
            return jsonObject.getDouble(paramName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Boolean booleanFromObject(JSONObject jsonObject, String key) {
        try {
            return jsonObject.getBoolean(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}

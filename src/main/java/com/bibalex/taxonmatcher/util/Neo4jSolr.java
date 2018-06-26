package com.bibalex.taxonmatcher.util;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Neo4jSolr {

    public String getString (JSONObject obj, String key)
    {
        String value = String.valueOf(obj.get(key));
        return  value;
    }

    public int getInt (JSONObject obj, String key)
    {
        int value = (Integer)obj.get(key);
        return value;
    }

    public boolean isHybrid (JSONObject obj)
    {
        boolean is_hybrid = Boolean.valueOf(String.valueOf(obj.get("is_hybrid"))) ;
        return is_hybrid;
    }

    public ArrayList<String> getStringArray (JSONObject obj, String key)
    {
        ArrayList<String> value = (ArrayList<String>) obj.get(key);
        return value;
    }

    public ArrayList<Integer> getIntegerArray (JSONObject obj, String key)
    {
        ArrayList<Integer> value = (ArrayList<Integer>) obj.get(key);
        return value;
    }

    public Map<String, Object> mapToDoc(Object name)
    {
        Map<String, Object> fieldModifier = new HashMap<String, Object>(1);
        fieldModifier.put("set", name);
        return fieldModifier;
    }

}

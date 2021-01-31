package com.souher.sdk.extend;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class JsonBuilder {
    private JSONObject object = new JSONObject();

    public static JsonBuilder New() {
        return new JsonBuilder();
    }

    public JsonBuilder with(String key, Object value) {
        object.put(key, value);
        return this;
    }

    public JsonBuilder add(String key, JSONObject value) {
        JSONArray arr = new JSONArray();
        if (object.containsKey(key)) {
            arr = object.getJSONArray(key);
        }
        arr.add(value);
        object.put(key, arr);
        return this;
    }

    public JSONObject get()
    {
        return object;
    }
}

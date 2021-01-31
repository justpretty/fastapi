package com.souher.sdk.extend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class Pair<F,V>
{
    public F one;
    public V theother;

    @Override
    public String toString()
    {
        return JSON.toJSONString(this, SerializerFeature.WRITE_MAP_NULL_FEATURES);
    }
}

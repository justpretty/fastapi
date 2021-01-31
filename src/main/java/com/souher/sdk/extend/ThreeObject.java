package com.souher.sdk.extend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.souher.sdk.iApp;

public class ThreeObject<A,B,C>
{
    public A first;
    public B second;
    public C third;

    public static ThreeObject Of(Object first,Object second,Object third)
    {
        ThreeObject threeObject=new ThreeObject();
        threeObject.first=first;
        threeObject.second=second;
        threeObject.third=third;

        return threeObject;
    }

    @Override
    public String toString()
    {
        return JSON.toJSONString(this, SerializerFeature.WRITE_MAP_NULL_FEATURES);
    }
}

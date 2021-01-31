package com.souher.sdk.extend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.souher.sdk.iApp;

public class FourObject<A,B,C,D>
{
    public A first;
    public B second;
    public C third;
    public D fourth;

    public static FourObject Of(Object first, Object second, Object third,Object fourth)
    {
        FourObject object=new FourObject();
        object.first=first;
        object.second=second;
        object.third=third;
        object.fourth=fourth;
        return object;
    }

    @Override
    public String toString()
    {
        return JSON.toJSONString(this, SerializerFeature.WRITE_MAP_NULL_FEATURES);
    }
}

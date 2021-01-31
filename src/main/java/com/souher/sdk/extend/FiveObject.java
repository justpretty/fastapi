package com.souher.sdk.extend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class FiveObject<A,B,C,D,E>
{
    public A first;
    public B second;
    public C third;
    public D fourth;
    public E fifth;

    public static FiveObject Of(Object first, Object second, Object third, Object fourth,Object fifth)
    {
        FiveObject object=new FiveObject();
        object.first=first;
        object.second=second;
        object.third=third;
        object.fourth=fourth;
        object.fifth=fifth;
        return object;
    }

    @Override
    public String toString()
    {
        return JSON.toJSONString(this, SerializerFeature.WRITE_MAP_NULL_FEATURES,SerializerFeature.PrettyFormat);
    }
}

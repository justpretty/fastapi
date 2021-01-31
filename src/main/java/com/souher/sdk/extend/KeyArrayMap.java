package com.souher.sdk.extend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class KeyArrayMap<E,T> {

    public  ConcurrentHashMap<E, ArrayList<T>> data = new ConcurrentHashMap<>();

    public void append(E key,T value)
    {
        synchronized (this) {
            ArrayList<T> a = new ArrayList<T>();
            if(data.containsKey(key))
            {
                a= data.get(key);
            }
            a.add(value);
            data.put(key,a);
        }
    }

    public KeyArrayMap<E,T> clone()
    {
        KeyArrayMap<E,T> keyArrayMap=new KeyArrayMap<>();
        data.forEach((a, b)->{
            b.forEach(c->{
                keyArrayMap.append(a,c);
            });
        });
        return keyArrayMap;
    }
    public void foreach(BiConsumer<? super E,? super ArrayList<T>> action)
    {
        data.forEach(action);
    }

    public Enumeration<E> keys()
    {
        return data.keys();
    }

    public int size()
    {
        return data.size();
    }
    public ArrayList<T> get(E key)
    {
        return data.get(key);
    }
    public void remove(E key)
    {
        data.remove(key);
    }
    public boolean containsKey(E key)
    {
        return data.containsKey(key);
    }
    public boolean containsObject(E key,T value)
    {
        if(!data.containsKey(key))
        {
            return false;
        }
        ArrayList<T> list = data.get(key);
        if(list ==null)
        {
            return false;
        }
        if(list.contains(value))
        {
            return true;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return JSON.toJSONString(this, SerializerFeature.WRITE_MAP_NULL_FEATURES,SerializerFeature.PrettyFormat);
    }
}

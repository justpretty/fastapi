package com.souher.sdk.extend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class KeyMapMap<E,F,T> {

    public  ConcurrentHashMap<E, ConcurrentHashMap<F,T>> data = new ConcurrentHashMap<>();

    public void put(E key, F key2, T value)
    {
        synchronized (this) {
            ConcurrentHashMap<F,T> a = new ConcurrentHashMap();
            if(data.containsKey(key))
            {
                a= data.get(key);
            }
            a.put(key2,value);
            data.put(key,a);
        }
    }


    public void put(E key,ConcurrentHashMap<F,T> val)
    {
        synchronized (this) {
            data.put(key,val);
        }
    }
    public void foreach(BiConsumer<? super E,? super ConcurrentHashMap<F,T>> action)
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
    public ConcurrentHashMap<F,T> get(E key)
    {
        return data.get(key);
    }
    public T get(E key,F key2)
    {
        ConcurrentHashMap<F,T> a= data.get(key);
        if(a==null||!a.containsKey(key2))
        {
            return null;
        }
        return a.get(key2);
    }

    public void remove(E key)
    {
        data.remove(key);
    }
    public boolean containsKey(E key)
    {
        return data.containsKey(key);
    }
    public boolean containsKey2(E key,F key2)
    {
        if(!data.containsKey(key))
        {
            return false;
        }
        ConcurrentHashMap<F,T> list = data.get(key);
        if(list ==null)
        {
            return false;
        }
        if(!list.containsKey(key2))
        {
            return false;
        }
        return true;
    }
    public boolean containsObject(E key,F key2,T value)
    {
        if(!data.containsKey(key))
        {
            return false;
        }
        ConcurrentHashMap<F,T> list = data.get(key);
        if(list ==null)
        {
            return false;
        }
        if(!list.containsKey(key2))
        {
            return false;
        }
        T value2=list.get(key2);
        if(value2==null&&value!=null)
        {
            return false;
        }
        if(value2.equals(value))
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

    public static KeyMapMap of(Object... obj)
    {
        KeyMapMap keyMapMap=new KeyMapMap();
        for (int i = 0; i < obj.length; i+=3)
        {
            keyMapMap.put(obj[i],obj[i+1],obj[i+2]);
        }
        return keyMapMap;
    }
}

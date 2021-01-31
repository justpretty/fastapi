package com.souher.sdk.extend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class KeyPairMap<E,T,V> {

    private  ConcurrentHashMap<E, Pair<T,V>> InterfaceClassMap = new ConcurrentHashMap<>();

    public void append(E key,T one,V other)
    {
        synchronized (this) {
            Pair<T,V> a = new Pair<T,V>();
            if(InterfaceClassMap.containsKey(key))
            {
                a=InterfaceClassMap.get(key);
            }
            a.one=one;
            a.theother =other;
            InterfaceClassMap.put(key,a);
        }
    }
    public void foreach(BiConsumer<? super E,? super Pair<T,V>> action)
    {
        InterfaceClassMap.forEach(action);
    }

    public Enumeration<E> keys()
    {
        return InterfaceClassMap.keys();
    }

    public int size()
    {
        return InterfaceClassMap.size();
    }
    public Pair<T,V> get(E key)
    {
        return InterfaceClassMap.get(key);
    }
    public void remove(E key)
    {
        InterfaceClassMap.remove(key);
    }
    public boolean containsKey(E key)
    {
        return InterfaceClassMap.containsKey(key);
    }
    public boolean containsObject(E key,T one,V theother)
    {
        if(!InterfaceClassMap.containsKey(key))
        {
            return false;
        }
        Pair<T,V> list = InterfaceClassMap.get(key);
        if(list ==null)
        {
            return false;
        }
        Pair<T,V> pair=new Pair<T,V>();
        pair.one=one;
        pair.theother =theother;
        if(JSON.toJSONString(pair).equals(JSON.toJSONString(list)))
        {
            return true;
        }

        return false;
    }

    @Override
    public String toString()
    {
        return JSON.toJSONString(this, SerializerFeature.WRITE_MAP_NULL_FEATURES);
    }
}

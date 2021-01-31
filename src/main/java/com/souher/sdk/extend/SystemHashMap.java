package com.souher.sdk.extend;

import com.souher.sdk.database.DataResult;

import java.util.concurrent.ConcurrentHashMap;

public class SystemHashMap<K,R> extends ConcurrentHashMap<K,R>
{
    public static SystemHashMap instance()
    {
        return new SystemHashMap();
    }

    public SystemHashMap<K,R> of(K k1,R r1,Object... s)
    {
        this.put(k1,r1);

        if(s.length>=2)
        {
            DataResult dataResult=DataResult.castArray(s);
            K key=(K)(dataResult.remove(0));
            R val=(R)(dataResult.remove(0));
            this.of(key,val,dataResult.toArray());
        }
        return this;
    }



}

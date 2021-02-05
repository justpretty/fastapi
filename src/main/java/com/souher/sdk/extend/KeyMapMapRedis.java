package com.souher.sdk.extend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.iRedis;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class KeyMapMapRedis
{
    private String tag="";

    public KeyMapMap<String,String,String> getAll()
    {
        KeyMapMap<String,String,String> data=new KeyMapMap<>();
        foreach(data::put);
        return data;
    }

    public KeyMapMapRedis(String tag)
    {
        this.tag=tag;
    }
    public String jedisKey(String key)
    {
        return  tag + ":" + key;
    }
    private String myKey(String rediskey)
    {
        int index=tag.length();
        return rediskey.substring(index+1);
    }

    public void increase(String key,String key2,long number)
    {
        synchronized (this) {
            ConcurrentHashMap<String,String> a = new ConcurrentHashMap();
            if(containsKey(key))
            {
                a= get(key);
            }
            long count=0;
            if(a.containsKey(key2))
            {
                count= Long.parseLong(a.get(key2));
            }
            count+=number;
            a.put(key2, String.valueOf(count));
            put(key,a);
        }
    }
    public void put(String key, String key2, String value)
    {
        ConcurrentHashMap<String,String> a = new ConcurrentHashMap<>();
        Jedis jedis= iRedis.getJedis();
        jedis.hset(jedisKey(key),key2,value);
        jedis.close();
    }
    public void put(String key,ConcurrentHashMap<String,String> val)
    {
        Jedis jedis= iRedis.getJedis();
        jedis.hset(jedisKey(key),val);
        jedis.close();
    }
    public void foreach(BiConsumer<String, ConcurrentHashMap<String, String>> action)
    {
        Jedis jedis= iRedis.getJedis();
        Set<String> sets=jedis.keys(jedisKey("*"));
        sets.forEach(a->{
            action.accept(myKey(a), new ConcurrentHashMap<>(jedis.hgetAll(a)));
        });
        jedis.close();
    }



    public ArrayList<String> keys()
    {
        Jedis jedis= iRedis.getJedis();
        Set<String> sets=jedis.keys(jedisKey("*"));
        ArrayList<String> list=new ArrayList<>();
        sets.forEach(a->{
            list.add(myKey(a));
        });
        jedis.close();
        return list;
    }

    public int size()
    {
        Jedis jedis= iRedis.getJedis();
        Set<String> sets=jedis.keys(jedisKey("*"));
        jedis.close();
        return sets.size();
    }
    public void clear()
    {
        keys().forEach(this::remove);
    }
    public ConcurrentHashMap<String,String> get(String key)
    {
        Jedis jedis= iRedis.getJedis();
        ConcurrentHashMap<String,String> map=new ConcurrentHashMap<>(jedis.hgetAll(jedisKey(key)));
        jedis.close();
        return map;
    }
    public String get(String key,String key2)
    {
        Jedis jedis= iRedis.getJedis();
        String a=jedis.hget(jedisKey(key),key2);
        jedis.close();
        return a;
    }

    public void remove(String key)
    {
        Jedis jedis= iRedis.getJedis();
        jedis.del(jedisKey(key));
        jedis.close();
    }
    public boolean containsKey(String key)
    {
        Jedis jedis= iRedis.getJedis();
        boolean flag= jedis.exists(jedisKey(key));
        jedis.close();
        return flag;
    }
    public boolean containsObject(String key,String key2,String value)
    {
        Jedis jedis= iRedis.getJedis();
        String val=jedis.hget(key,key2);
        jedis.close();
        if(val==null)
        {
            return value==null;
        }
        return value.equals(val);
    }

    @Override
    public String toString()
    {
        return JSON.toJSONString(this, SerializerFeature.WRITE_MAP_NULL_FEATURES,SerializerFeature.PrettyFormat);
    }
}

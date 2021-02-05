package com.souher.sdk.extend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.souher.sdk.iApp;
import com.souher.sdk.iRedis;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class KeyRedis
{

    private String tag="";

    public KeyRedis(String tag)
    {
        this.tag=tag;
    }
    private String jedisKey(String key)
    {
        return  tag + ":" + key;
    }
    private String myKey(String rediskey)
    {
        int index=tag.length();
        return rediskey.substring(index+1);
    }
    private String myKey1(String rediskey)
    {
        int index=rediskey.lastIndexOf(":");
        return rediskey.substring(index+1);
    }
    private String myKey2(String rediskey)
    {
        String[] arr=rediskey.split(":");
        return arr[arr.length-2];
    }

    public void put(String key,String value)
    {
        Jedis jedis= iRedis.getJedis();
        jedis.set(jedisKey(key),value);
        jedis.close();
        iApp.debug("redis."+tag+".added",key);
    }

    public ArrayList<String> keys(String pattern)
    {
        Jedis jedis= iRedis.getJedis();
        Set<String> sets=jedis.keys(jedisKey(pattern));
        ArrayList<String> list=new ArrayList<>();
        sets.forEach(a->{
            list.add(myKey(a));
        });
        jedis.close();
        return list;
    }

    public ConcurrentHashMap<String,String> keys2(String pattern)
    {
        Jedis jedis= iRedis.getJedis();
        Set<String> sets=jedis.keys(jedisKey(pattern));
        ConcurrentHashMap<String,String> list=new ConcurrentHashMap<>();
        sets.forEach(a->{
            list.put(myKey1(a),myKey2(a));
        });
        jedis.close();
        return list;
    }

    public int size()
    {
        Jedis jedis= iRedis.getJedis();
        Set<String> sets=jedis.keys(jedisKey("*"));
        int size=sets.size();
        jedis.close();
        return size;
    }

    public String get(String key)
    {
        Jedis jedis= iRedis.getJedis();
        String jedisKey=jedisKey(key);
        String list=jedis.get(jedisKey);
        jedis.close();
        return list;
    }
    public void remove(String key)
    {
        Jedis jedis= iRedis.getJedis();
        jedis.del(jedisKey(key));
        jedis.close();
        iApp.debug("redis."+tag+".removed",key);
    }
    public boolean containsKey(String key)
    {
        Jedis jedis= iRedis.getJedis();
        boolean flag= jedis.exists(jedisKey(key));
        jedis.close();
        return flag;
    }
    public boolean containsObject(String key,String value)
    {
        return get(key).contains(value);
    }

    @Override
    public String toString()
    {
        return JSON.toJSONString(this, SerializerFeature.WRITE_MAP_NULL_FEATURES,SerializerFeature.PrettyFormat);
    }
}

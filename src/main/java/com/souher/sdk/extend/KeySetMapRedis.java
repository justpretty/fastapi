package com.souher.sdk.extend;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.souher.sdk.iRedis;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class KeySetMapRedis
{

    private String tag="";

    public KeySetMapRedis(String tag)
    {
        this.tag=tag;
    }
    private String jedisKey(String key)
    {
        return  tag + ":" + key;
    }
    private String myKey(String rediskey)
    {
        int index=rediskey.lastIndexOf(":");
        return rediskey.substring(index+1);
    }

    public void append(String key,String... value)
    {
        Jedis jedis= iRedis.getJedis();
        jedis.sadd(jedisKey(key),value);
        jedis.close();
    }

    public void foreach(BiConsumer<String, Set<String>> action)
    {
        Jedis jedis= iRedis.getJedis();
        Set<String> sets=jedis.keys(jedisKey("*"));
        sets.forEach(a->{
            action.accept(myKey(a),jedis.smembers(a));
        });
        jedis.close();
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

    public int size()
    {
        Jedis jedis= iRedis.getJedis();
        Set<String> sets=jedis.keys(jedisKey("*"));
        int size=sets.size();
        jedis.close();
        return size;
    }

    public Set<String> get(String key)
    {
        Jedis jedis= iRedis.getJedis();
        String jedisKey=jedisKey(key);
        Set<String> list=jedis.smembers(jedisKey);
        jedis.close();
        return list;
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

package com.souher.sdk;

import com.souher.sdk.database.DataModel;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ListPosition;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface iRedis
{
    JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();


    JedisPool pool=iAppConfig.redisConfig().password().isEmpty()
            ?new JedisPool(jedisPoolConfig,iAppConfig.redisConfig().host(),iAppConfig.redisConfig().port(),100000){{
        jedisPoolConfig.setMaxTotal(100000);
        jedisPoolConfig.setMaxIdle(10000);
        jedisPoolConfig.setMaxWaitMillis(10000);
        jedisPoolConfig.setTestOnBorrow(true);
    }}
            :new JedisPool(jedisPoolConfig,iAppConfig.redisConfig().host(),iAppConfig.redisConfig().port(),100000,iAppConfig.redisConfig().password()){{
        jedisPoolConfig.setMaxTotal(100000);
        jedisPoolConfig.setMaxIdle(10000);
        jedisPoolConfig.setMaxWaitMillis(10000);
        jedisPoolConfig.setTestOnBorrow(true);
    }};


    static Jedis getJedis() {
        return pool.getResource();
    }


}

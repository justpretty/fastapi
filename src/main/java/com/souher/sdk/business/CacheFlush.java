package com.souher.sdk.business;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.souher.sdk.api.SelectApi;
import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.extend.ExtendThread;
import com.souher.sdk.extend.KeyMapMapRedis;
import com.souher.sdk.extend.KeySetMapRedis;
import com.souher.sdk.extend.Reflector;
import com.souher.sdk.iApp;
import com.souher.sdk.iRedis;
import com.souher.sdk.interfaces.*;
import com.souher.sdk.model.CacheTask;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class CacheFlush implements iOnEveryHour
{

    @Override
    public void onEveryHour(Long tick) throws Exception
    {
        Date date=new Date(tick);
        String hour=iApp.H.format(date);
        if(!hour.equals("3"))
        {
            return;
        }
        Jedis jedis= iRedis.getJedis();
        jedis.flushAll();
        jedis.close();
    }
}

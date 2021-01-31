package com.souher.sdk.business;

import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.iApp;
import com.souher.sdk.iRedis;
import com.souher.sdk.interfaces.iAllModelWatcher;
import com.souher.sdk.interfaces.iOnDataDeleted;
import com.souher.sdk.interfaces.iOnDataSaved;
import com.souher.sdk.interfaces.iRedisModel;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Map;

public class CacheModelWorker implements iOnDataSaved, iOnDataDeleted, iAllModelWatcher
{
    public static CacheModelWorker Current=new CacheModelWorker();

    @Override
    public void onDataDeleted(DataModel model)
    {
        iApp.debug(this.getClass().getSimpleName()+".onDataDeleted",model.toString());
        if(iRedisModel.class.isAssignableFrom(model.getClass()))
        {
            ((iRedisModel)model).removeFromRedis();
        }
    }

    @Override
    public void onDataSaved(DataModel model)
    {
        iApp.debug(this.getClass().getSimpleName()+".onDataSaved",model.toString());
        if(iRedisModel.class.isAssignableFrom(model.getClass()))
        {
            ((iRedisModel)model).removeFromRedis();
        }
    }

    public void clearAllCache(Class<? extends DataModel> cls)
    {
        if(!iRedisModel.class.isAssignableFrom(cls))
        {
            return;
        }
        String jedisKey = cls.getSimpleName() + ":*" ;
        Jedis jedis = iRedis.getJedis();
        ArrayList<String> keys = new ArrayList<>(DataResult.castE(jedis.keys(jedisKey)));
        iApp.debug(this.getClass().getSimpleName()+".clearAllCache",String.join(",",keys));
        if(keys.size()==0)
        {
            jedis.close();
            return;
        }
        jedis.del(keys.toArray(new String[0]));
        jedis.close();
    }
}

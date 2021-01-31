package com.souher.sdk.business;

import com.souher.sdk.database.DataModel;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.iApp;
import com.souher.sdk.iRedis;
import com.souher.sdk.interfaces.iOnEveryHour;
import com.souher.sdk.interfaces.iOnEveryMinute;
import com.souher.sdk.interfaces.iOnEveryMinuteForAll;
import com.souher.sdk.model.Session;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

public class SessionExpireWorker implements iOnEveryMinute
{
    @Override
    public void onEveryMinute(Long tick) throws Exception
    {
        long b=tick/1000/60;
        if(b%7!=0)
        {
            return;
        }
        DataResult<Session> result= DataModel.all(Session.class);
        result.forEachForcely(a->{
            if(a.expire_tick<tick)
            {
                a.delete();
            }
        });
    }
}

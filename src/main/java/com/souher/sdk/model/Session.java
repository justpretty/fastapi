package com.souher.sdk.model;

import com.souher.sdk.database.DataModel;
import com.souher.sdk.interfaces.iRedisModel;

public class Session extends DataModel implements iRedisModel
{
    public Integer id;
    public String key;
    public Integer refer_id;
    public Long expire_tick;

    public static int userid(String key) throws Exception
    {
        Session session=new Session();
        session.key=key;
        session.first();
        if(!session.hasId())
        {
            return 0;
        }
        return session.refer_id;
    }

    @Override
    public String[] keyColumn()
    {
        return new String[]{"key","id"};
    }

    public static Session save(String key, int id, long expire_in) throws Exception
    {
        Session session=new Session();
        session.key=key;
        session.first();
        session.key=key;
        session.refer_id=id;
        session.expire_tick=System.currentTimeMillis()+expire_in*1000;
        session.save();
        return session;
    }
}

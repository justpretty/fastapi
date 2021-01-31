package com.souher.sdk.model;

import com.souher.sdk.database.DataModel;

public class CacheTask extends DataModel
{
    public Integer id;
    public String api_tag;
    public String request;
    public Integer user_id;


    public static synchronized void save(String api_tag,String request,int user_id) throws Exception
    {
        CacheTask cacheTask=new CacheTask();
        cacheTask.request=request;
        cacheTask.user_id=user_id;
        cacheTask.api_tag=api_tag;
        cacheTask.first();
        cacheTask.save();
    }
}

package com.souher.sdk.api;

import com.souher.sdk.business.CacheModelWorker;
import com.souher.sdk.business.CacheRequestWorker;
import com.souher.sdk.database.DataModel;
import com.souher.sdk.extend.Reflector;
import com.souher.sdk.iApp;
import com.souher.sdk.interfaces.iApi;
import spark.Request;
import spark.Response;

import java.lang.reflect.InvocationTargetException;

public class UpdateCache implements iApi
{

    @Override
    public boolean hasPost()
    {
        return true;
    }

    @Override
    public boolean hasGet()
    {
        return true;
    }

    @Override
    public String urlPath()
    {
        return "/updatecache/*";
    }

    @Override
    public String handle(Request request, Response response)
    {
        String className=request.splat()[0];
        Class<? extends DataModel> cls= Reflector.Default.searchDataModelClass(className);
        if(cls==null)
        {
            return "error1";
        }

        try
        {
            CacheModelWorker.Current.clearAllCache(cls);
            CacheRequestWorker.initAsync();
        }
        catch (Exception e)
        {
            iApp.error(e);
        }

        return "success";
    }
}

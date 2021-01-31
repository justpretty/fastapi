package com.souher.sdk.api;

import com.souher.sdk.database.DataResult;
import com.souher.sdk.iApp;
import com.souher.sdk.iAppConfig;
import com.souher.sdk.interfaces.iApi;
import spark.Request;
import spark.Response;

public class AdminConfig implements iApi
{
    @Override
    public boolean hasPost()
    {
        return false;
    }

    @Override
    public boolean hasGet()
    {
        return true;
    }

    @Override
    public String urlPath()
    {
        return "/adminconfig/*/*";
    }

    @Override
    public String handle(Request request, Response response)
    {
        if(!DataResult.castArray(iAppConfig.webConfig().whiteiplist()).contains(request.ip()))
        {
            return "ip deny:"+request.ip();
        }
        String[] map= request.splat();
        if(map.length!=2)
        {
            return "参数需为2个";
        }
        switch (map[0])
        {
            case "debug":
                iApp.isDebug.set(Boolean.parseBoolean(map[1]));
                return "iApp.isDebug:" + iApp.isDebug.get();
        }
        return null;
    }
}

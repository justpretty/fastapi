package com.souher.sdk.api;

import com.souher.sdk.interfaces.iApi;
import spark.Request;
import spark.Response;

public class HelloWorld implements iApi
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
        return "/";
    }

    @Override
    public String handle(Request request, Response response)
    {
        return "Hello World:"+System.currentTimeMillis();
    }
}

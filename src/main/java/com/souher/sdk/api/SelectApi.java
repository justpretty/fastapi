package com.souher.sdk.api;

import com.souher.sdk.interfaces.iSelectApi;

public class SelectApi implements iSelectApi
{
    public boolean force=false;

    public SelectApi()
    {

    }

    public SelectApi(boolean force)
    {
        this.force=force;
    }

    @Override
    public boolean forceGet()
    {
        return force;
    }

    public static SelectApi forceSelectApi=new SelectApi(true);
}

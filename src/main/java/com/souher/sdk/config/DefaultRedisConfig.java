package com.souher.sdk.config;

import com.souher.sdk.extend.Reflector;
import com.souher.sdk.iApp;
import com.souher.sdk.interfaces.iAlarmer;
import com.souher.sdk.interfaces.iRedisConfig;

import java.util.ArrayList;

public class DefaultRedisConfig implements iRedisConfig
{
    private static iRedisConfig current=null;

    public static iRedisConfig Current()
    {
        if(current!=null)
        {
            return current;
        }
        ArrayList<Class> classes = Reflector.Default.getAllClassByInterface(iRedisConfig.class);
        classes.forEach(a->{
            if(!a.equals(DefaultRedisConfig.class))
            {
                try
                {
                    current= (iRedisConfig) a.newInstance();
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }
            }
        });
        if(current==null)
        {
            current=new DefaultRedisConfig();
        }
        return current;
    }

    @Override
    public String host()
    {
        return "127.0.0.1";
    }

    @Override
    public int port()
    {
        return 6379;
    }

    @Override
    public String password()
    {
        return "";
    }
}

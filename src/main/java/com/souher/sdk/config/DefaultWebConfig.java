package com.souher.sdk.config;

import com.souher.sdk.extend.Reflector;
import com.souher.sdk.iApp;
import com.souher.sdk.interfaces.iDatabaseConfig;
import com.souher.sdk.interfaces.iWebConfig;

import java.util.ArrayList;

public class DefaultWebConfig implements iWebConfig
{
    private static iWebConfig current=null;

    public static iWebConfig Current()
    {
        if(current!=null)
        {
            return current;
        }
        ArrayList<Class> classes = Reflector.Default.getAllClassByInterface(iWebConfig.class);
        classes.forEach(a->{
            if(!a.equals(DefaultWebConfig.class))
            {
                try
                {
                    current= (iWebConfig) a.newInstance();
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }
            }
        });
        if(current==null)
        {
            current=new DefaultWebConfig();
        }
        return current;
    }

    @Override
    public String[] whiteiplist()
    {
        return new String[]{"127.0.0.1"};
    }

    @Override
    public int threadPoolSize()
    {
        return 10000000;
    }

    @Override
    public String jksPath()
    {
        return "/java/ssl/a.jks";
    }

    @Override
    public String jksPassword()
    {
        return "jksPassword";
    }

    @Override
    public String staticLocation()
    {
        return "/tmp";
    }

    @Override
    public String apiVersion()
    {
        return "v1";
    }
}

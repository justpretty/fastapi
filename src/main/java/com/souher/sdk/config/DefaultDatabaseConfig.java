package com.souher.sdk.config;

import com.souher.sdk.extend.Reflector;
import com.souher.sdk.iApp;
import com.souher.sdk.interfaces.iDatabaseConfig;

import java.util.ArrayList;

public class DefaultDatabaseConfig implements iDatabaseConfig
{

    private static iDatabaseConfig current=null;

    public static iDatabaseConfig Current()
    {
        if(current!=null)
        {
            return current;
        }
        ArrayList<Class> classes = Reflector.Default.getAllClassByInterface(iDatabaseConfig.class);
        classes.forEach(a->{
            if(!a.equals(DefaultDatabaseConfig.class))
            {
                try
                {
                    current= (iDatabaseConfig) a.newInstance();
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }
            }
        });
        if(current==null)
        {
            current=new DefaultDatabaseConfig();
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
        return 80;
    }

    @Override
    public String user()
    {
        return "root";
    }

    @Override
    public String password()
    {
        return "root";
    }

    @Override
    public String database()
    {
        return "mysql";
    }

    @Override
    public String prefix()
    {
        return "la_";
    }
}

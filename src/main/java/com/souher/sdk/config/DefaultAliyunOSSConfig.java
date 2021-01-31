package com.souher.sdk.config;

import com.souher.sdk.extend.Reflector;
import com.souher.sdk.iApp;
import com.souher.sdk.interfaces.iAlarmer;
import com.souher.sdk.interfaces.iAliyunOSSConfig;

import java.util.ArrayList;

public class DefaultAliyunOSSConfig implements iAliyunOSSConfig
{
    private static iAliyunOSSConfig current=null;

    public static iAliyunOSSConfig Current()
    {
        if(current!=null)
        {
            return current;
        }
        ArrayList<Class> classes = Reflector.Default.getAllClassByInterface(iAliyunOSSConfig.class);
        classes.forEach(a->{
            if(!a.equals(DefaultAliyunOSSConfig.class))
            {
                try
                {
                    current= (iAliyunOSSConfig) a.newInstance();
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }
            }
        });
        if(current==null)
        {
            current=new DefaultAliyunOSSConfig();
        }
        return current;
    }


    @Override
    public String endpoint()
    {
        return "https://oss-cn-hangzhou.aliyuncs.com";
    }

    @Override
    public String appid()
    {
        return "appid";
    }

    @Override
    public String appsecret()
    {
        return "appsecret";
    }

    @Override
    public String bucketname()
    {
        return "bucketname";
    }

    @Override
    public String domain()
    {
        return "domain";
    }
}

package com.souher.sdk.config;

import com.souher.sdk.extend.Reflector;
import com.souher.sdk.iApp;
import com.souher.sdk.interfaces.iAlarmer;
import com.souher.sdk.interfaces.iWechatConfig;

import java.util.ArrayList;

public class DefaultWechatConfig implements iWechatConfig
{
    private static iWechatConfig current=null;

    public static iWechatConfig Current()
    {
        if(current!=null)
        {
            return current;
        }
        ArrayList<Class> classes = Reflector.Default.getAllClassByInterface(iWechatConfig.class);
        classes.forEach(a->{
            if(!a.equals(DefaultWechatConfig.class))
            {
                try
                {
                    current= (iWechatConfig) a.newInstance();
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }
            }
        });
        if(current==null)
        {
            current=new DefaultWechatConfig();
        }
        return current;
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
}

package com.souher.sdk.config;

import com.souher.sdk.extend.Reflector;
import com.souher.sdk.iApp;
import com.souher.sdk.interfaces.iAlarmer;
import com.souher.sdk.interfaces.iWechatUser;

import java.util.ArrayList;

public class DefaultWechatUser implements iWechatUser
{
    private static iWechatUser current=null;

    public static iWechatUser Current()
    {
        if(current!=null)
        {
            return current;
        }
        ArrayList<Class> classes = Reflector.Default.getAllClassByInterface(iWechatUser.class);
        classes.forEach(a->{
            if(!a.equals(DefaultWechatUser.class))
            {
                try
                {
                    current= (iWechatUser) a.newInstance();
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }
            }
        });
        if(current==null)
        {
            current=new DefaultWechatUser();
        }
        return current;
    }

    @Override
    public boolean checkEnabled()
    {
        //enable return true ,disable return false

        return true;
    }

    @Override
    public iWechatUser userLoginByOpenid(String openid) throws Exception
    {
        //get the user by openid

        return this;
    }

    @Override
    public iWechatUser userSaveInfo(int id, String nickname, String pic, int gender, String country, String province, String city, String language) throws Exception
    {
        //save the user's info

        return this;
    }
}

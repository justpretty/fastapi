package com.souher.sdk.config;

import com.souher.sdk.extend.Reflector;
import com.souher.sdk.iApp;
import com.souher.sdk.interfaces.iAlarmer;
import com.souher.sdk.interfaces.iWebConfig;

import java.util.ArrayList;

public class DefaultAlarmer implements iAlarmer
{
    private static iAlarmer current=null;

    public static iAlarmer Current()
    {
        if(current!=null)
        {
            return current;
        }
        ArrayList<Class> classes = Reflector.Default.getAllClassByInterface(iAlarmer.class);
        classes.forEach(a->{
            if(!a.equals(DefaultAlarmer.class))
            {
                try
                {
                    current= (iAlarmer) a.newInstance();
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }
            }
        });
        if(current==null)
        {
            current=new DefaultAlarmer();
        }
        return current;
    }


    @Override
    public void alarm(String message)
    {

    }
}

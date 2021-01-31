package com.souher.sdk.api;

import com.souher.sdk.extend.Reflector;
import com.souher.sdk.interfaces.iAdminCommand;
import com.souher.sdk.iApp;
import com.souher.sdk.interfaces.iApi;
import spark.Request;
import spark.Response;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;

public class DebugApi implements iApi
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
        return "/admin/cmd/*";
    }

    @Override
    public String handle(Request request, Response response)
    {
        if(!iApp.isDebug.get())
        {
            return "非DEBUG模式！";
        }

        ArrayList<Class> classes = Reflector.Default.getAllClassByInterface(iAdminCommand.class);
        String result="";
        for (int i = 0; i < classes.size(); i++)
        {
            Class cls = classes.get(i);
            if (cls.getSimpleName().equals(request.splat()[0]))
            {
                try
                {
                    result=((iAdminCommand)cls.newInstance()).main();
                }
                catch (Exception e)
                {
                    iApp.error(e);
                }

                break;
            }
        }
        if(result==null||result.isEmpty())
        {
            result = "executed!";
        }

        return result;
    }
}

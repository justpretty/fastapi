package com.souher.sdk.business;

import com.souher.sdk.database.DataModel;
import com.souher.sdk.iApp;
import com.souher.sdk.iFile;
import com.souher.sdk.interfaces.iOnEveryMinuteForAll;
import com.souher.sdk.model.DatabaseStatus;

import java.io.File;

public class ConfigLoader implements iOnEveryMinuteForAll
{
    @Override
    public void onEveryMinuteForAll(Long tick) throws Exception
    {
        if(iFile.exists("debug"))
        {
            String a=iFile.readString("debug");
            if(a==null)
            {
                return;
            }
            a=a.trim();
            if(a.equals("true")&&!iApp.isDebug.get())
            {
                iApp.isDebug.set(true);
                iApp.print("is debug is set to true!");
            }
            else if(a.equals("false")&&iApp.isDebug.get())
            {
                iApp.isDebug.set(false);
                iApp.print("is debug is set to false!");
            }
            else if(!a.equals("true")&&!a.equals("false"))
            {
                iApp.print("is debug is invalid!:"+a);
            }
        }
        else
        {
            iFile.write(new File("debug"), String.valueOf(iApp.isDebug.get()));
        }

    }
}

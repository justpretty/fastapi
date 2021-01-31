package com.souher.sdk.extend;

import com.souher.sdk.iApp;

import java.util.function.Function;
import java.util.function.Supplier;

public interface iForceTry
{

    static <R> R run(SystemSupplier<R> fun)
    {
        return run(10,60,fun);
    }

    static <R> R run(int times,long intervalTick,SystemSupplier<R> fun)
    {
        R result=null;
        while(times-->0)
        {
            try
            {
                result=fun.get();
                break;
            }
            catch (Exception e)
            {
                iApp.error(e);
                try
                {
                    Thread.sleep(intervalTick);
                }
                catch (InterruptedException interruptedException)
                {
                    iApp.error(interruptedException);
                }
            }
        }
        return result;
    }
}

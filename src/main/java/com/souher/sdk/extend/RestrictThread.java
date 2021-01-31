package com.souher.sdk.extend;

import com.souher.sdk.iApp;

import java.util.concurrent.ConcurrentHashMap;

public class RestrictThread
{

    private static KeyArrayMap<String,Runnable> cache=new KeyArrayMap<>();
    private static KeyArrayMap<String,Runnable> isRunning=new KeyArrayMap<>();
    private static ConcurrentHashMap<String,Long> sizeMap=new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String,Boolean> runingStage=new ConcurrentHashMap<>();
    private String key;

    public RestrictThread(String key, long size, Runnable runnable)
    {
        sizeMap.put(key,size);
        this.key=key;
        cache.append(key,runnable);
    }
    public void start()
    {
        triggerRun(key);
    }

    public static void triggerRun(String key)
    {
        run(key);
    }

    private static void run(String key)
    {
        Runnable a = null;
        synchronized (key)
        {
            if (!cache.containsKey(key) || cache.get(key).size() == 0)
            {
                return;
            }

            if (isRunning.containsKey(key) && isRunning.get(key).size() >= sizeMap.get(key))
            {
                int sizes = 0;
                if (isRunning.containsKey(key))
                {
                    sizes = isRunning.get(key).size();
                }
                iApp.debug("SIZE:" + sizes);
                return;
            }
            a = cache.get(key).get(0);
            cache.get(key).remove(0);
            isRunning.append(key, a);
        }
        if (a == null)
        {
            return;
        }
        Runnable finalA = a;
        new ExtendThread(() ->
        {
            try
            {
                finalA.run();
            }
            catch (Exception e)
            {
                iApp.error(e);
            }
            synchronized (key)
            {
                isRunning.get(key).remove(finalA);
            }
            run(key);
        }).start();
    }
}

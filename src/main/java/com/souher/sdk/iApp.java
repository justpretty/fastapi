package com.souher.sdk;

import com.souher.sdk.business.CacheRequestWorker;
import com.souher.sdk.database.DataResult;
import com.souher.sdk.extend.InterfaceExecutor;
import com.souher.sdk.extend.Reflector;
import com.souher.sdk.interfaces.iApi;
import com.souher.sdk.interfaces.iOnAppReady;
import com.souher.sdk.task.CronTask;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static spark.Spark.*;
import static spark.Spark.get;

public interface iApp
{
    SimpleDateFormat yyyyMMddHHmmssSSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat EEEE = new SimpleDateFormat("EEEE", Locale.ENGLISH);
    SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat yyyyMMdd_WithOutMinus = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat HH = new SimpleDateFormat("HH");
    SimpleDateFormat HHmm = new SimpleDateFormat("HH:mm");
    SimpleDateFormat H = new SimpleDateFormat("H");
    SimpleDateFormat m = new SimpleDateFormat("m");
    SimpleDateFormat ChineseDate = new SimpleDateFormat("yyyy年MM月dd日");
    SimpleDateFormat ChineseMonthDay = new SimpleDateFormat("MM月dd日");
    SimpleDateFormat ChineseShortMonthDay = new SimpleDateFormat("M月d日");
    SimpleDateFormat ChineseDateTime = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
    SimpleDateFormat ChineseDateAndWeekDay = new SimpleDateFormat("yyyy年MM月dd日(EEEE)", Locale.CHINA);
    SimpleDateFormat yyyy = new SimpleDateFormat("yyyy");

    AtomicReference<String> debugHead=new AtomicReference<String>("");
    AtomicBoolean isDebug = new AtomicBoolean(true);

    static SimpleDateFormat format(String str)
    {
        return new SimpleDateFormat(str);
    }

    static int weekNumber(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);//设置星期一为一周开始的第一天
//        calendar.setMinimalDaysInFirstWeek(4);//可以不用设置
        calendar.setTimeInMillis(date.getTime());//获得当前的时间戳
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);//获得当前日期属于今年的第几周
        return weekOfYear;
    }
    static void debug(String... message)
    {
        if (isDebug.get())
        {
            print(message);
        }
    }

    static void print(String... message)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < message.length; i++)
        {
            if (sb.length() > 0)
            {
                sb.append("=====>");
            }
            sb.append(message[i]);
        }
        print(sb.toString());
    }

    static void print(String message)
    {
        System.out.println("["+debugHead.get()+"]"+yyyyMMddHHmmssSSS.format(new Date()) + ">>>>>>" + message);
    }

    static void alarm(String message)
    {
        iAppConfig.alarmer().alarm(message);
    }

    static void error(Exception e)
    {
        String message = e.getMessage() + Arrays.toString(e.getStackTrace());
        error(message);
    }

    static void error(Exception e, String head)
    {
        String message = head + "\n" + e.getMessage() + Arrays.toString(e.getStackTrace());
        error(message);
    }

    static void error(String message)
    {
        if (message == null)
        {
            message = "";
        }
        String text = yyyyMMddHHmmssSSS.format(new Date()) + "---ERROR:" + message;
        System.err.println(text);
        iApp.alarm(message);
    }

    static void warning(Exception e)
    {
        String message = e.getMessage() + Arrays.toString(e.getStackTrace());
        warning(message);
    }

    static void warning(String message)
    {
        System.err.println(yyyyMMddHHmmssSSS.format(new Date()) + "---WARNING:" + message);
    }

    static File JarFile()
    {
        if(App.jarFile!=null)
        {
            return App.jarFile;
        }
        try
        {
            Class<?> cls=Class.forName("sun.launcher.LauncherHelper");
            Method[] methods=cls.getDeclaredMethods();
            Method method=null;
            DataResult<String> methodList=new DataResult<>();
            for (int i = 0; i < methods.length; i++)
            {
                if(methods[i].getName().equals("getApplicationClass")
                &&methods[i].getParameterCount()==0
                        && Modifier.isStatic(methods[i].getModifiers())
                )
                {
                    method=methods[i];
                    methodList.add(methods[i].getName());
                }
            }
            Object obj=  method.invoke(null);
            Class<?> cls1= (Class<?>) obj;
            App.mainClass=cls1;
            ProtectionDomain cls2=cls1.getProtectionDomain();
            CodeSource codeSource=cls2.getCodeSource();
            URL url=codeSource.getLocation();
            String path=url.getPath();
            path = java.net.URLDecoder.decode(path, "UTF-8"); // 转换处理中文及空格
            iApp.debug("jarFile:",path);
            App.jarFile=new File(path);
           // iApp.debug("jarFile2:",App.jarFile.getAbsolutePath());
            return App.jarFile;
        }
        catch (Exception e)
        {
            return null;
        }

    }

    static void init(String[] args)
    {
        System.setProperty("https.protocols", "TLSv1.2");
        staticFiles.externalLocation(iAppConfig.webConfig().staticLocation());
        if (args.length > 0)
        {
            debugHead.set(args[0]);
            port(Integer.parseInt(args[0]));
        }
        else
        {
            port(443);
            String keyStoreLocation = iAppConfig.webConfig().jksPath();
            String keyStorePassword = iAppConfig.webConfig().jksPassword();
            secure(keyStoreLocation, keyStorePassword, null, null);
        }
        threadPool(iAppConfig.webConfig().threadPoolSize());

        ArrayList<Class> classes = Reflector.Default.getAllClassByInterface(iApi.class);
        handleApiClasses(classes);
        if (args.length > 1&&args[1].equals("default"))
        {
            CronTask.Current.init();
            CacheRequestWorker.initAsync();
        }
        CronTask.Current.initForAll();
        InterfaceExecutor.Current.executorOrderly(iOnAppReady.class);
    }

    static void handleApiClasses(ArrayList<Class> classes)
    {
        for (Class iWebClass : classes)
        {
            if (iWebClass.isInterface())
            {
                ArrayList<Class> classes1 = Reflector.Default.getAllClassByInterface(iWebClass);
                handleApiClasses(classes1);
                continue;
            }
            iApi web = null;
            try
            {
                web = (iApi) iWebClass.newInstance();
                iApi finalIWeb = web;
                if (finalIWeb.hasPost())
                {
                    iApp.debug("----" + finalIWeb.getClass().getSimpleName() + ":" + web.urlPath());
                    post(web.urlPath(), (request, response) ->
                    {
                        iApp.debug(request.uri() + ":" + ":" + finalIWeb.getClass().getSimpleName());
                        request.attribute("__from__","outside");
                        String result = finalIWeb.handle(request, response);

                        if (result == null)
                        {
                            return response;
                        }
                        return result;
                    });
                }
                if (finalIWeb.hasGet())
                {
                    get(web.urlPath(), (request, response) ->
                    {
                        request.attribute("__from__","outside");
                        String result = finalIWeb.handle(request, response);
                        if (result == null)
                        {
                            return response;
                        }
                        return result;
                    });
                }
            }
            catch (Exception e)
            {
                iApp.error(e);
            }
        }
    }
}

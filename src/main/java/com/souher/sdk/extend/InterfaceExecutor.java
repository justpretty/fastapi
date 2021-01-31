package com.souher.sdk.extend;

import com.souher.sdk.database.DataResult;
import com.souher.sdk.iApp;
import com.souher.sdk.interfaces.iAllModelWatcher;
import com.souher.sdk.interfaces.iMultiModelWatcher;
import com.souher.sdk.interfaces.iOnHasErrorMessage;
import com.souher.sdk.interfaces.iUrgent;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;

public class InterfaceExecutor {
    public static InterfaceExecutor Current = new InterfaceExecutor();
    public Reflector reflector = Reflector.Default;

    public void check(Class interfaceClass,Object... object) throws Exception
    {
         check( interfaceClass, null,object);
    }
    public void check(Class interfaceClass,String methodName,Object... object) throws Exception
    {

        ArrayList<Class> classes=reflector.getAllClassByInterface(interfaceClass);
        if(methodName==null)
        {
            Method executeMethod=interfaceClass.getMethods()[0];
            methodName=executeMethod.getName();
        }
        String finalMethodName = methodName;

        for(Class cls :classes)
        {
            if(cls.isInterface())
            {
                continue;
            }
            Object instance = cls.newInstance();
            Method[] methods = cls.getMethods();
            for (Method method : methods)
            {
                if (Modifier.isVolatile(method.getModifiers()))
                {
                    continue;
                }
                if (!method.getName().equals(finalMethodName))
                {
                    continue;
                }
                if (method.getParameterCount() != object.length)
                {
                    iApp.debug("参数长度不一致：" + cls.getName() + ":" + method.getName());
                    continue;
                }
                String pClassName = "";
                if (method.getParameterCount() > 0)
                {
                    Parameter[] parameterTypes = method.getParameters();
                    boolean badParameter = false;
                    for (int i = 0; i < parameterTypes.length; i++)
                    {
                        Class<?> a = parameterTypes[i].getType();
                        pClassName = object[i].getClass().getSimpleName();
                        if (!a.equals(object[i].getClass()))
                        {
                            if (a.isAssignableFrom(object[i].getClass()) && iMultiModelWatcher.class.isAssignableFrom(cls)
                                    && DataResult.castArray(((iMultiModelWatcher) instance).otherWatchedClasses()).contains(object[i].getClass())
                            )
                            {
                                continue;
                            }
                            badParameter = true;
                            break;
                        }
                    }
                    if (badParameter)
                    {
                        continue;
                    }
                }
                iApp.debug("InterfaceExecutor checking", "checking 【" + cls.getSimpleName() + "." + finalMethodName + "】 ...  " + pClassName + ":" + Arrays.toString(object));
                method.invoke(instance, object);
                break;
            }
        }
    }

    public void executorLimited(boolean isMainThead,String methodName,long threadSize,ArrayList<String> preRestrictedTypes, Class interfaceClass,Object... object) {

        ArrayList<Class> classes=reflector.getAllClassByInterface(interfaceClass);

        StringBuilder sb = new StringBuilder();
        classes.forEach(a->sb.append(a.getSimpleName()+"|"));

        if(methodName==null)
        {
            Method executeMethod=interfaceClass.getMethods()[0];
            methodName=executeMethod.getName();
        }
        String finalMethodName = methodName;


        for(Class cls :classes)
        {
            ArrayList<String> restrictedTypes=new ArrayList<>();
            restrictedTypes.addAll(preRestrictedTypes);
            Type[] types=cls.getGenericInterfaces();
            for (int i = 0; i < types.length; i++)
            {
                Type b=types[i];
                if(b instanceof ParameterizedType)
                {
                    Type[] c=((ParameterizedType) b).getActualTypeArguments();
                    for (int i1 = 0; i1 < c.length; i1++)
                    {
                        restrictedTypes.add(c[i1].getTypeName());
                    }
                }
            }

            if(cls.isInterface())
            {
                if(!cls.equals(interfaceClass))
                {
                    //restrictedTypes.clear();
                    executorLimited(isMainThead,methodName,threadSize,restrictedTypes,cls,object);
                }
                continue;
            }

            Runnable runnable=() ->
            {
                try
                {
                    Object instance = cls.newInstance();
                    Method[] methods = cls.getMethods();
                    for (Method method : methods)
                    {
                        if(Modifier.isVolatile(method.getModifiers()))
                        {
                            continue;
                        }
                        if (method.getName().equals(finalMethodName))
                        {
                            if(method.getParameterCount()!=object.length)
                            {
                                iApp.error("参数长度不一致："+cls.getName()+":"+method.getName());
                                continue;
                            }
                            String pClassName="";
                            if(method.getParameterCount()>0)
                            {
                                Parameter[] parameterTypes=method.getParameters();
                                boolean badParameter=false;
                                for(int i=0;i<parameterTypes.length;i++)
                                {
                                    Class<?> a= parameterTypes[i].getType();
                                    pClassName=object[i].getClass().getSimpleName();
                                    if(!a.equals(object[i].getClass()) && !restrictedTypes.contains(object[i].getClass().getName()) )
                                    {
                                        iApp.debug("InterfaceExecutor.executorLimited."+new Throwable().getStackTrace()[0].getLineNumber(),cls.getSimpleName()+":"+a.getSimpleName()+":"+object[i].getClass());
                                        if(a.isAssignableFrom(object[i].getClass()) && iAllModelWatcher.class.isAssignableFrom(cls))
                                        {
                                            iApp.debug("InterfaceExecutor.executorLimited."+new Throwable().getStackTrace()[0].getLineNumber(),cls.getSimpleName()+":"+a.getSimpleName()+":"+object[i].getClass()+":iAllModelWatcher_continued");
                                            continue;
                                        }

                                        if(a.isAssignableFrom(object[i].getClass()) && iMultiModelWatcher.class.isAssignableFrom(cls)
                                                && DataResult.castArray(((iMultiModelWatcher)instance).otherWatchedClasses()).contains(object[i].getClass())
                                        )
                                        {
                                            continue;
                                        }
                                        badParameter=true;
                                        break;
                                    }
                                }
                                if(badParameter)
                                {
                                    continue;
                                }
                            }
                            try
                            {
                                iApp.debug("InterfaceExecutor.executorLimited","executing 【" + cls.getSimpleName() + "." + finalMethodName + "】 ...  "+pClassName+":"+Arrays.toString(object));
                                method.invoke(instance, object);
                            }
                            catch (Exception e)
                            {
                                String err=cls.getName() + "|" + method.getName();
                                if(e.getCause()!=null)
                                {
                                    if(e.getCause().getMessage()!=null)
                                    {
                                        err+="|"+e.getCause().getMessage();
                                    }
                                    if(e.getCause().getStackTrace()!=null)
                                    {
                                        err+="|"+Arrays.toString(e.getCause().getStackTrace());
                                    }
                                }
                                iApp.error(e,err);
                            }
                            break;
                        }
                    }
                }
                catch (Exception e)
                {
                    iApp.error(e, cls.getName());
                }
            };
            if(isMainThead)
            {
                runnable.run();
            }
            else if(iUrgent.class.isAssignableFrom(interfaceClass))
            {
                new Thread(runnable).start();
            }
            else if(threadSize==0)
            {
                new ExtendThread(runnable).start();
            }
            else
            {
                iApp.debug(this.getClass().getSimpleName()+".executorLimited.RestrictThread","threadSize:"+threadSize);
                new RestrictThread(interfaceClass.getName(),threadSize,runnable).start();
            }
        }
    }
    public void executor(Class interfaceClass,Object... object) {
        executorLimited(false,null,0,new ArrayList<>(), interfaceClass, object);
    }
    public void executorMethod(Class interfaceClass,String methodName,Object... object) {
        executorLimited(false,methodName,0, new ArrayList<>(),interfaceClass, object);
    }
    public void executorOrderly(Class interfaceClass,Object... object) {
        executorLimited(true,null,0, new ArrayList<>(),interfaceClass, object);
    }

    public void executorMethodOrderly(Class interfaceClass,String methodName,Object... object) {
        executorLimited(true,methodName,0,new ArrayList<>(), interfaceClass, object);
    }

}

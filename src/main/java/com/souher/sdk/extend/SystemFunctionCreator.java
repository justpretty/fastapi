package com.souher.sdk.extend;



public class SystemFunctionCreator<T,R>
{
     public static <T,R> SystemFunctionCreator<T,R> instance(Class<T> tclass,Class<R> rclass)
     {
          return new SystemFunctionCreator<T,R>();
     }
     public SystemFunction<T, R> of(SystemFunction<T, R> c)
     {
          return c;
     }

}
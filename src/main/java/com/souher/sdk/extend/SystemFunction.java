package com.souher.sdk.extend;


import com.souher.sdk.database.DataResult;

@FunctionalInterface
public interface SystemFunction<T,R>{
     R accept (T t) throws Exception;


}

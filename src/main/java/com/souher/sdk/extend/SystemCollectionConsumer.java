package com.souher.sdk.extend;


import com.souher.sdk.database.DataResult;

@FunctionalInterface
public interface SystemCollectionConsumer<T>{

     void accept (DataResult<T> t) throws Exception;
     

}

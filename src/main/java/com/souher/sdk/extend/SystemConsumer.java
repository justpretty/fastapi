package com.souher.sdk.extend;

@FunctionalInterface
public interface SystemConsumer<T>{

     void accept (T t) throws Exception;
}

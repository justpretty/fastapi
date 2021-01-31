package com.souher.sdk.interfaces;

public interface iRedisConfig
{
    default String host(){
        return "127.0.0.1";
    }
    default int port(){
        return 6379;
    }
    default String password()
    {
        return "";
    }
}

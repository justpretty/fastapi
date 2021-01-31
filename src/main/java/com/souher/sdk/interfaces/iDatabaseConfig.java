package com.souher.sdk.interfaces;

public interface iDatabaseConfig
{
    String host();
    int port();
    String user();
    String password();
    String database();
    String prefix();

    default boolean autoTableStruct()
    {
        return true;
    }
    default boolean debugSql()
    {
        return true;
    }
    default int initPoolSize()
    {
        return 100;
    }
    default int minPoolSize()
    {
        return 100;
    }
    default int maxPoolSize()
    {
        return 1000;
    }
    default int acquireIncrement()
    {
        return 10;
    }
}

package com.souher.sdk;

import com.souher.sdk.config.*;
import com.souher.sdk.interfaces.*;

public interface iAppConfig
{
    static iDatabaseConfig databaseConfig()
    {
        return DefaultDatabaseConfig.Current();
    }

    static iWebConfig webConfig()
    {
        return DefaultWebConfig.Current();
    }

    static iAlarmer alarmer()
    {
        return DefaultAlarmer.Current();
    }

    static iAliyunOSSConfig aliyunOSSConfig(){
        return DefaultAliyunOSSConfig.Current();
    }

    static iWechatConfig wechatConfig()
    {
        return DefaultWechatConfig.Current();
    }

    static iWechatUser newUser() {
        return DefaultWechatUser.Current();
    }

    static iRedisConfig redisConfig(){
        return DefaultRedisConfig.Current();
    }
}
